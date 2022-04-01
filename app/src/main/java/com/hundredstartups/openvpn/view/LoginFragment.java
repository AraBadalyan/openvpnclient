package com.hundredstartups.openvpn.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hundredstartups.openvpn.R;
import com.hundredstartups.openvpn.SharedPreference;
import com.hundredstartups.openvpn.api.APIClient;
import com.hundredstartups.openvpn.api.APIInterface;
import com.hundredstartups.openvpn.cache.UserAccountManager;
import com.hundredstartups.openvpn.model.GoogleSignInRequestBody;

import retrofit2.Call;
import retrofit2.Callback;

public class LoginFragment extends BaseFragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

    private SharedPreference mPreference;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1010;
    private APIInterface mApiInterface;
    private String mToken;


    private FirebaseAuth mFirebaseAuth;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_login, container, false);
        getToken();
        mFirebaseAuth = FirebaseAuth.getInstance();
        init();
        checkPlayServices();
        initGoogle();
        mApiInterface = new APIClient().getClient().create(APIInterface.class);
//        SignInButton signInButton = view.findViewById(R.id.btn_google_sign_in);
//        signInButton.setSize(SignInButton.SIZE_WIDE);
        view.findViewById(R.id.btn_google_sign_in).setOnClickListener(v -> signInGoogle());
        return view;
    }

    private void init() {
        mPreference = new SharedPreference(getContext());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                    Task<GetTokenResult> tokenResultTask= user.getIdToken(true);
                    onGetGoogleTokenSuccess(tokenResultTask.getResult().getToken());


//                    onGoogleSuccess(user);
                } else {
                    // If sign in fails, display a message to the user.
//                    AlertManager.showErrorMessage(mActivity, getString(R.string.server_error))
                }
            }
        });
    }


    private void onGetGoogleTokenSuccess(String token) {
        GoogleSignInRequestBody googleSignInRequestBody = new GoogleSignInRequestBody();
        googleSignInRequestBody.setId_token(token);
        googleSignInRequestBody.setDevice_token_id(mToken);
        Call<Void> call = mApiInterface.sendGoogleToken(googleSignInRequestBody);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.headers().get("Token") != null) {
                    UserAccountManager.INSTANCE.saveUserData(response.headers().get("Token"), response.headers().get("UserId"), response.headers().get("Email"));
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).init();
                    } else {
                        UserAccountManager.INSTANCE.saveUserData(null, null, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void initGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    private void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
//            AlertManager.showErrorMessage(getActivity(), getString(R.string.toast_google_play_services_err));
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, 9000).show();
            } else {
                getActivity().finish();
            }
        }
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    mToken = task.getResult();
                });
    }
}