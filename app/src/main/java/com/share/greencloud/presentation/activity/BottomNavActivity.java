package com.share.greencloud.presentation.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.share.greencloud.R;
import com.share.greencloud.common.BaseActivity;
import com.share.greencloud.databinding.ActivityBottomNavBinding;
import com.share.greencloud.databinding.NavHeaderMainBinding;
import com.share.greencloud.domain.login.LoginManager;
import com.share.greencloud.domain.model.User;
import com.share.greencloud.presentation.fragment.MapFragment;
import com.share.greencloud.presentation.fragment.WeatherFragment;
import com.share.greencloud.presentation.viewmodel.NavHeaderViewModel;
import com.share.greencloud.utils.GreenCloudPreferences;
import com.tbruyelle.rxpermissions2.RxPermissions;

import timber.log.Timber;

public class BottomNavActivity extends BaseActivity<ActivityBottomNavBinding> implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        WeatherFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private ActivityBottomNavBinding binding;
    private NavHeaderViewModel viewModel;
    private BottomSheetBehavior bottomSheetBehavior;

    private final Fragment[] childFragment = new Fragment[]{
            new MapFragment(),
            new WeatherFragment()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate is called");

        setupView();

        if (!checkPermissions()) {
            getLocationPermission();
        }
    }

    private void setupView() {
        binding = getBinding();
        binding.setLifecycleOwner(this);
        setupToolbar();
        setupDrawerNavView();
        setupBottomNavView();
//        setupViewModel();
        if (LoginManager.getInstance().isLogin()) {
            bindUserProfile(); // 로그인 중에만 프로필 정보 불러오도록
        }
        changeTrasparentColorToolbarAndStatusbar();
        loadDefaultFragment();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupViewModel() {

//        ViewModelFactory viewModelFactory = new ViewModelFactory();
//        viewModel = ViewModelProviders.of(this).get(NavHeaderViewModel.class);
//        mapFragmentViewModel = ViewModelProviders.of(this).get(MapFragmentViewModel.class);
    }

    private void setupDrawerNavView() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        DrawerArrowDrawable arrow = toggle.getDrawerArrowDrawable();
        arrow.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        binding.navView.setNavigationItemSelectedListener(this);

    }

    private void setupDrawerNavClickListen() {
        // 네비게이션뷰 메뉴 클릭 시 이동 이벤트
        findViewById(R.id.ll_history).setOnClickListener(v -> {
            binding.drawerLayout.closeDrawers();
            Intent intent = new Intent(this, UserHistoryActivity.class);
            new Handler().postDelayed(() ->
                    startActivity(intent), 300);
        });
        findViewById(R.id.ll_news).setOnClickListener(v -> {
            binding.drawerLayout.closeDrawers();
            Intent intent = new Intent(this, GreenNewsActivity.class);
            new Handler().postDelayed(() ->
                    startActivity(intent), 300);
        });
        findViewById(R.id.ll_weather).setOnClickListener(v -> {
            binding.drawerLayout.closeDrawers();
            new Handler().postDelayed(() ->
                    loadFragment(childFragment[1]), 300);
        });

        findViewById(R.id.ll_rent_loc).setOnClickListener(v -> {
            binding.drawerLayout.closeDrawers();
            new Handler().postDelayed(() ->
                    loadFragment(childFragment[0]), 300);
        });
    }

    private void setupBottomNavView() {
//        binding.bottomNavView.setOnNavigationItemSelectedListener(this);
        bottomSheetBehavior = BottomSheetBehavior.from(binding.rlRentalInfo);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        binding.rentalInfo.setBottomNavActivity(this);
    }

    // databing을 활용하여 프로필 정보를 연결
    void bindUserProfile() {
        NavHeaderMainBinding headerMainBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0));

        String userName = GreenCloudPreferences.getUserId(this);
        String userProfileImg = GreenCloudPreferences.getUserProfileImage(this);
        User user = new User(userName, userProfileImg);
        if (headerMainBinding.getViewModel() == null) {
            headerMainBinding.setViewModel(
                    new NavHeaderViewModel(user));
        }
    }

    private void changeTrasparentColorToolbarAndStatusbar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void loadDefaultFragment() {
        loadFragment(childFragment[0]);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.disallowAddToBackStack();
        transaction.setReorderingAllowed(true);
        transaction.commit();
//        invalidateOptionsMenu(); // 메뉴 아이템 변경 시 호출해야함.
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocationPermission() {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        reloadActivity();
                    } else {
                        Toast.makeText(this, "위치정보 사용에 대한 동의가 거부되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reloadActivity() {
        Timber.d("reloadActivity is called");
        Intent intent = getIntent();
        finish();
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        startActivity(intent);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        Timber.d("onCreateOptionsMenu is called");
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//
////        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
////        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
////        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
////        searchView.setQueryHint("대여소 검색...");
//
//
//        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//        searchView.isSubmitButtonEnabled();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                if(query != null) {
//                    getItemFromDB("test");
//                }
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//
//                return false;
//            }
//        });
//
//
//        observeSearchMenu(menu);
//
//        return true;
//    }

    private void observeSearchMenu(Menu menu) {
        viewModel.getHideSearchMenu().observe(this, (hideSearchMenu) -> {
            if (hideSearchMenu) {
                changeMenuItemVisible(menu, false);
            } else {
                changeMenuItemVisible(menu, true);
            }
        });
    }

    private void changeMenuItemVisible(Menu menu, Boolean visibility) {
        menu.findItem(R.id.menu_search).setVisible(visibility);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_favorite_places:
                //todo 추후 구현 예정
                return true;

            case R.id.navigation_green_news:
                Intent intent = new Intent(this, GreenNewsActivity.class);
                startActivity(intent);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.navigation_usage_history:
                Intent intentHistory = new Intent(this, UserHistoryActivity.class);
                startActivity(intentHistory);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.navigation_setting:
            //todo 추후 구현 예정
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultFragment();
        Timber.d("onResume is called");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void showBottomSlide() {
        bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ? BottomSheetBehavior.STATE_HIDDEN : BottomSheetBehavior.STATE_EXPANDED);
    }

    public void hideBottomSlide(View view) {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void scanQRcode(View view) {
        hideBottomSlide(view);
        Intent intent = new Intent(this, QRScanActivity.class);
        startActivity(intent);
    }

    public void search(View view) {
        hideBottomSlide(view);
        Intent intent = new Intent(this, SearchResultActivity.class);
        startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_bottom_nav;
    }
}
