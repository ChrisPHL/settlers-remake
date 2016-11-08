package jsettlers.main.android.fragmentsnew;

import jsettlers.main.android.GameService;
import jsettlers.main.android.R;
import jsettlers.main.android.dialogs.DirectoryPickerDialog;
import jsettlers.main.android.navigation.MainMenuNavigator;
import jsettlers.main.android.resources.scanner.ResourceLocationScanner;
import jsettlers.main.android.utils.FragmentUtil;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainMenuFragment extends Fragment implements DirectoryPickerDialog.Listener {
	private static final int REQUEST_CODE_PERMISSION_STORAGE = 10;

	private MainMenuNavigator navigator;
	private boolean showDirectoryPicker = false;
	private boolean resourcesLoaded = false;

	private LinearLayout mainLinearLayout;
	private View resourcesView;
	private View resumeView;

	private boolean bound = false;

	public static MainMenuFragment newInstance() {
		return new MainMenuFragment();
	}

	public MainMenuFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		navigator = (MainMenuNavigator) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
		FragmentUtil.setActionBar(this, view);

		mainLinearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_main);
		resumeView = view.findViewById(R.id.card_view_resume);

		View resumeButton = view.findViewById(R.id.button_resume);
		resumeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				navigator.resumeGame();
			}
		});

		if (!tryLoadResources()) {
			resourcesView = inflater.inflate(R.layout.include_resources_card, mainLinearLayout, false);
			mainLinearLayout.addView(resourcesView, 0);

			Button button = (Button) resourcesView.findViewById(R.id.button_resources);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDirectoryPicker();
				}
			});
		}

		Button newSingleGameButton = (Button) view.findViewById(R.id.button_new_single_game);
		newSingleGameButton.setOnClickListener(new GameButtonClickListener() {
			@Override
			protected void doAction() {
				navigator.showNewSinglePlayerMapPicker();
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Work around for IllegalStateException when trying to show dialog from onPermissionResult. Meant to be fixed in v24 support library
		if (showDirectoryPicker) {
			showDirectoryPicker();
			showDirectoryPicker = false;
		}

		getActivity().bindService(new Intent(getActivity(), GameService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (bound) {
			getActivity().unbindService(serviceConnection);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDirectorySelected() {
		if (tryLoadResources()) {
			mainLinearLayout.removeView(resourcesView);
		} else {
			throw new RuntimeException("Resources not found or not valid after directory chosen by user");
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
		case REQUEST_CODE_PERMISSION_STORAGE:
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				showDirectoryPicker = true;
			}
			break;
		}
	}

	private boolean tryLoadResources() {
		resourcesLoaded = new ResourceLocationScanner(getActivity()).scanForResources();
		return resourcesLoaded;
	}

	private void showDirectoryPicker() {
		if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_CODE_PERMISSION_STORAGE);
		} else {
			DirectoryPickerDialog.newInstance().show(getChildFragmentManager(), null);
		}
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			GameService.GameBinder gameBinder = (GameService.GameBinder) binder;
			GameService gameService = gameBinder.getService();

			bound = true;

			if (gameService.isGameInProgress()) {
				resumeView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
		}
	};

	private abstract class GameButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (resourcesLoaded) {
				doAction();
			}
		}

		protected abstract void doAction();
	}
}
