package cn.boweikeji.explorer;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CLICK_INTERVAL = 500;

    private EditText.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Log.d(TAG, "onEditorAction: " + actionId);
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String url = mEditText.getText().toString();
                if (isValidUrl(url)) {
                    mWebView.loadUrl(url);
                    mWebView.requestFocus();
                }
            }
            return true;
        }
    };

    @ViewById(R.id.top_layout)
    ViewGroup mTopLayout;
    @ViewById(R.id.edittext)
    EditText mEditText;
    @ViewById(R.id.progress)
    ProgressBar mProgressBar;
    @ViewById(R.id.bottom_layout)
    ViewGroup mBottomLayout;
    @ViewById(R.id.back_btn)
    ImageButton mBackBtn;
    @ViewById(R.id.forward_btn)
    ImageButton mForwardBtn;
    @ViewById(R.id.refresh_btn)
    ImageButton mRefreshBtn;
    @ViewById(R.id.home_btn)
    ImageButton mHomeBtn;
    @ViewById(R.id.webview)
    WebView mWebView;

    @Pref
    HomePref_ mHomePref;

    private long mExitTime;

    @AfterViews
    protected void afterViews() {
        initView();
        bindEvent();
        goHome();
    }

    private void initView() {
        mEditText.setImeActionLabel(getString(R.string.go), EditorInfo.IME_ACTION_DONE);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                updateUrl(url);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    if (mProgressBar.getVisibility() == View.GONE) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void bindEvent() {
        mEditText.setOnEditorActionListener(mOnEditorActionListener);

        clickEvent(mBackBtn, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                goBack();
            }
        });

        clickEvent(mForwardBtn, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                goForward();
            }
        });

        clickEvent(mRefreshBtn, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                refresh();
            }
        });

        clickEvent(mHomeBtn, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                goHome();
            }
        });

        longClickEvent(mHomeBtn, new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                changeHome();
            }
        });
    }

    private void goBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    private void goForward() {
        if (mWebView.canGoForward()) {
            mWebView.goForward();
        }
    }

    private void refresh() {
        mWebView.reload();
    }

    private void goHome() {
        String homepage = mHomePref.homepage().get();
        updateUrl(homepage);

        mWebView.clearHistory();
        mWebView.loadUrl(homepage);
    }

    private void changeHome() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.set_homepage)
                .setMessage(R.string.set_homepage_msg)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = mWebView.getUrl();
                        if (isValidUrl(url)) {
                            mHomePref.homepage().put(url);
                            Toast.makeText(getApplicationContext(), R.string.change_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.change_success, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void clickEvent(View view, Action1<Void> action) {
        RxView.clicks(view)
                .throttleFirst(CLICK_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(action);
    }

    private void longClickEvent(View view, Action1<Void> action) {
        RxView.longClicks(view)
                .throttleFirst(CLICK_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(action);
    }

    private void updateUrl(String url) {
        mEditText.setText(url);
        mEditText.setSelection(mEditText.getText().toString().length());
    }

    private boolean isValidUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
