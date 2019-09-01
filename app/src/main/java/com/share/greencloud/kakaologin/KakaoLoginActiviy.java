package com.share.greencloud.kakaologin;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.share.greencloud.R;
import com.share.greencloud.databinding.ActivityKakaoLoginActiviyBinding;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class KakaoLoginActiviy extends AppCompatActivity {
    private SessionCallback callback;
    private KakaoLoginViewModel viewModel;
    private KakaoLoginVeiwModelFactory veiwModelFactory;
    private ActivityKakaoLoginActiviyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_kakao_login_activiy);

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        Session.getCurrentSession().checkAndImplicitOpen();

        veiwModelFactory = new KakaoLoginVeiwModelFactory();
        viewModel = ViewModelProviders.of(this, veiwModelFactory).get(KakaoLoginViewModel.class);

        binding.comKakaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, KakaoLoginActiviy.this);
            }
        });

    }

    public class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            if (Session.getCurrentSession().isOpened()) { // 한 번더 세션을 체크해주었습니다.
                requestMe();
            }

        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if (exception != null) {
                Timber.e(exception);
            }
        }
    }

    private void requestMe() {
        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("kakao_account.email");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Timber.d(message);
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Timber.i("onSessionClosed: %s", errorResult);
            }

            @Override
            public void onSuccess(MeV2Response response) {
                Timber.i("KaKaLogin Success: %s", response.toString());

                Toast.makeText(KakaoLoginActiviy.this, response.getNickname() + "님이 카카오 간편 로그인에 성공하였습니다!", Toast.LENGTH_SHORT).show();
                // todo: Access Token 서버 전달
                String accessToken = Session.getCurrentSession().getTokenInfo().getAccessToken();
                viewModel.saveAccessToken(accessToken);
                //viewModel.saveShared(response.getNickname(), response.getKakaoAccount().getEmail(), response.getProfileImagePath());
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }
}
