/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.print.PrintManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Intents.Insert;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.WebViewTransport;
import android.webkit.WebViewDatabase;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.item.AutoPageScrollAction;
import jp.hazuki.yuzubrowser.action.item.CloseAutoSelectAction;
import jp.hazuki.yuzubrowser.action.item.CloseTabSingleAction;
import jp.hazuki.yuzubrowser.action.item.CustomMenuSingleAction;
import jp.hazuki.yuzubrowser.action.item.CustomSingleAction;
import jp.hazuki.yuzubrowser.action.item.FinishSingleAction;
import jp.hazuki.yuzubrowser.action.item.GoBackSingleAction;
import jp.hazuki.yuzubrowser.action.item.LeftRightTabSingleAction;
import jp.hazuki.yuzubrowser.action.item.MousePointerSingleAction;
import jp.hazuki.yuzubrowser.action.item.OpenOptionsMenuAction;
import jp.hazuki.yuzubrowser.action.item.OpenUrlSingleAction;
import jp.hazuki.yuzubrowser.action.item.PasteGoSingleAction;
import jp.hazuki.yuzubrowser.action.item.PasteSearchBoxAction;
import jp.hazuki.yuzubrowser.action.item.SaveScreenshotSingleAction;
import jp.hazuki.yuzubrowser.action.item.ShareScreenshotSingleAction;
import jp.hazuki.yuzubrowser.action.item.ShowSearchBoxAction;
import jp.hazuki.yuzubrowser.action.item.TabListSingleAction;
import jp.hazuki.yuzubrowser.action.item.ToastAction;
import jp.hazuki.yuzubrowser.action.item.TranslatePageSingleAction;
import jp.hazuki.yuzubrowser.action.item.VibrationSingleAction;
import jp.hazuki.yuzubrowser.action.item.WebScrollSingleAction;
import jp.hazuki.yuzubrowser.action.item.WithToastAction;
import jp.hazuki.yuzubrowser.action.item.startactivity.StartActivitySingleAction;
import jp.hazuki.yuzubrowser.action.manager.DoubleTapFlickActionManager;
import jp.hazuki.yuzubrowser.action.manager.FlickActionManager;
import jp.hazuki.yuzubrowser.action.manager.HardButtonActionManager;
import jp.hazuki.yuzubrowser.action.manager.LongPressActionManager;
import jp.hazuki.yuzubrowser.action.manager.MenuActionManager;
import jp.hazuki.yuzubrowser.action.manager.QuickControlActionManager;
import jp.hazuki.yuzubrowser.action.manager.TabActionManager;
import jp.hazuki.yuzubrowser.action.manager.WebSwipeActionManager;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.action.view.ActionListViewAdapter;
import jp.hazuki.yuzubrowser.adblock.AdBlockActivity;
import jp.hazuki.yuzubrowser.adblock.AdBlockController;
import jp.hazuki.yuzubrowser.adblock.AddAdBlockDialog;
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.bookmark.view.AddBookmarkKt;
import jp.hazuki.yuzubrowser.bookmark.view.BookmarkActivity;
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.browser.FinishAlertDialog;
import jp.hazuki.yuzubrowser.browser.HttpAuthRequestDialog;
import jp.hazuki.yuzubrowser.browser.SafeFileProvider;
import jp.hazuki.yuzubrowser.browser.openable.BrowserOpenable;
import jp.hazuki.yuzubrowser.debug.DebugActivity;
import jp.hazuki.yuzubrowser.download.DownloadDialog;
import jp.hazuki.yuzubrowser.download.DownloadFileProvider;
import jp.hazuki.yuzubrowser.download.DownloadListActivity;
import jp.hazuki.yuzubrowser.download.DownloadRequestInfo;
import jp.hazuki.yuzubrowser.download.DownloadService;
import jp.hazuki.yuzubrowser.download.FastDownloadActivity;
import jp.hazuki.yuzubrowser.favicon.FaviconAsyncManager;
import jp.hazuki.yuzubrowser.favicon.FaviconManager;
import jp.hazuki.yuzubrowser.gesture.GestureManager;
import jp.hazuki.yuzubrowser.gesture.multiFinger.data.MultiFingerGestureItem;
import jp.hazuki.yuzubrowser.gesture.multiFinger.data.MultiFingerGestureManager;
import jp.hazuki.yuzubrowser.gesture.multiFinger.detector.MultiFingerGestureDetector;
import jp.hazuki.yuzubrowser.gesture.multiFinger.detector.MultiFingerGestureInfo;
import jp.hazuki.yuzubrowser.gesture.view.GestureFrameLayout;
import jp.hazuki.yuzubrowser.history.BrowserHistoryActivity;
import jp.hazuki.yuzubrowser.history.BrowserHistoryAsyncManager;
import jp.hazuki.yuzubrowser.history.BrowserHistoryManager;
import jp.hazuki.yuzubrowser.menuwindow.MenuWindow;
import jp.hazuki.yuzubrowser.pattern.action.OpenOthersPatternAction;
import jp.hazuki.yuzubrowser.pattern.action.WebSettingPatternAction;
import jp.hazuki.yuzubrowser.pattern.action.WebSettingResetAction;
import jp.hazuki.yuzubrowser.pattern.url.PatternUrlActivity;
import jp.hazuki.yuzubrowser.pattern.url.PatternUrlChecker;
import jp.hazuki.yuzubrowser.pattern.url.PatternUrlManager;
import jp.hazuki.yuzubrowser.reader.ReaderActivity;
import jp.hazuki.yuzubrowser.readitlater.ReadItLaterActivity;
import jp.hazuki.yuzubrowser.readitlater.ReadItLaterKt;
import jp.hazuki.yuzubrowser.resblock.ResourceBlockListActivity;
import jp.hazuki.yuzubrowser.resblock.ResourceBlockManager;
import jp.hazuki.yuzubrowser.resblock.ResourceChecker;
import jp.hazuki.yuzubrowser.search.SearchActivity;
import jp.hazuki.yuzubrowser.search.SearchUtils;
import jp.hazuki.yuzubrowser.search.SuggestProvider;
import jp.hazuki.yuzubrowser.settings.PreferenceConstants;
import jp.hazuki.yuzubrowser.settings.activity.MainSettingsActivity;
import jp.hazuki.yuzubrowser.settings.container.ToolbarVisibilityContainter;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.preference.ClearBrowserDataAlertDialog;
import jp.hazuki.yuzubrowser.settings.preference.ProxySettingDialog;
import jp.hazuki.yuzubrowser.speeddial.SpeedDialAsyncManager;
import jp.hazuki.yuzubrowser.speeddial.SpeedDialHtml;
import jp.hazuki.yuzubrowser.speeddial.view.SpeedDialSettingActivity;
import jp.hazuki.yuzubrowser.tab.TabListLayout;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.tab.manager.OnWebViewCreatedListener;
import jp.hazuki.yuzubrowser.tab.manager.TabData;
import jp.hazuki.yuzubrowser.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.tab.manager.TabManager;
import jp.hazuki.yuzubrowser.tab.manager.TabManagerFactory;
import jp.hazuki.yuzubrowser.theme.ThemeData;
import jp.hazuki.yuzubrowser.toolbar.ToolbarManager;
import jp.hazuki.yuzubrowser.toolbar.sub.GeolocationPermissionToolbar;
import jp.hazuki.yuzubrowser.toolbar.sub.WebViewFindDialog;
import jp.hazuki.yuzubrowser.toolbar.sub.WebViewFindDialogFactory;
import jp.hazuki.yuzubrowser.toolbar.sub.WebViewPageFastScroller;
import jp.hazuki.yuzubrowser.useragent.UserAgentListActivity;
import jp.hazuki.yuzubrowser.userjs.UserScript;
import jp.hazuki.yuzubrowser.userjs.UserScriptDatabase;
import jp.hazuki.yuzubrowser.userjs.UserScriptListActivity;
import jp.hazuki.yuzubrowser.utils.ClipboardUtils;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.FileUtils;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.MathUtils;
import jp.hazuki.yuzubrowser.utils.PackageUtils;
import jp.hazuki.yuzubrowser.utils.PermissionUtils;
import jp.hazuki.yuzubrowser.utils.UrlUtils;
import jp.hazuki.yuzubrowser.utils.WebDownloadUtils;
import jp.hazuki.yuzubrowser.utils.WebUtils;
import jp.hazuki.yuzubrowser.utils.WebViewUtils;
import jp.hazuki.yuzubrowser.utils.app.LongPressFixActivity;
import jp.hazuki.yuzubrowser.utils.graphics.SimpleLayerDrawable;
import jp.hazuki.yuzubrowser.utils.graphics.TabListActionTextDrawable;
import jp.hazuki.yuzubrowser.utils.handler.PauseHandler;
import jp.hazuki.yuzubrowser.utils.view.CopyableTextView;
import jp.hazuki.yuzubrowser.utils.view.CustomCoordinatorLayout;
import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector;
import jp.hazuki.yuzubrowser.utils.view.PointerView;
import jp.hazuki.yuzubrowser.utils.view.SeekBarDialog;
import jp.hazuki.yuzubrowser.utils.view.behavior.WebViewBehavior;
import jp.hazuki.yuzubrowser.utils.view.pie.PieControlBase;
import jp.hazuki.yuzubrowser.utils.view.pie.PieMenu;
import jp.hazuki.yuzubrowser.utils.view.tab.TabLayout;
import jp.hazuki.yuzubrowser.utils.view.webviewfastscroll.WebViewFastScroller;
import jp.hazuki.yuzubrowser.webencode.WebTextEncodeListActivity;
import jp.hazuki.yuzubrowser.webkit.CustomOnCreateContextMenuListener;
import jp.hazuki.yuzubrowser.webkit.CustomWebBackForwardList;
import jp.hazuki.yuzubrowser.webkit.CustomWebChromeClient;
import jp.hazuki.yuzubrowser.webkit.CustomWebHistoryItem;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;
import jp.hazuki.yuzubrowser.webkit.CustomWebView.OnWebStateChangeListener;
import jp.hazuki.yuzubrowser.webkit.CustomWebViewClient;
import jp.hazuki.yuzubrowser.webkit.NormalWebView;
import jp.hazuki.yuzubrowser.webkit.TabType;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;
import jp.hazuki.yuzubrowser.webkit.WebCustomViewHandler;
import jp.hazuki.yuzubrowser.webkit.WebUploadHandler;
import jp.hazuki.yuzubrowser.webkit.WebViewAutoScrollManager;
import jp.hazuki.yuzubrowser.webkit.WebViewFactory;
import jp.hazuki.yuzubrowser.webkit.WebViewProvider;
import jp.hazuki.yuzubrowser.webkit.WebViewProvider.CachedWebViewProvider;
import jp.hazuki.yuzubrowser.webkit.WebViewProxy;
import jp.hazuki.yuzubrowser.webkit.WebViewRenderingManager;
import jp.hazuki.yuzubrowser.webkit.WebViewType;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageCopyUrlHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageLinkHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageLoadUrlHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageOpenBackgroundHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageOpenNewTabHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageOpenOtherAppHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageOpenRightBgTabHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageOpenRightNewTabHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcImageShareWebHandler;
import jp.hazuki.yuzubrowser.webkit.handler.WebSrcLinkCopyHandler;

public class BrowserActivity extends LongPressFixActivity implements WebBrowser, GestureOverlayView.OnGestureListener,
        GestureOverlayView.OnGesturePerformedListener, MenuWindow.OnMenuCloseListener, AddAdBlockDialog.OnAdBlockListUpdateListener,
        FinishAlertDialog.OnFinishDialogCallBack, OnWebViewCreatedListener, CachedWebViewProvider {
    private static final String TAG = "BrowserActivity";

    private static final int RESULT_REQUEST_WEB_UPLOAD = 1;
    private static final int RESULT_REQUEST_SEARCHBOX = 2;
    private static final int RESULT_REQUEST_BOOKMARK = 3;
    private static final int RESULT_REQUEST_HISTORY = 4;
    private static final int RESULT_REQUEST_SETTING = 5;
    private static final int RESULT_REQUEST_USERAGENT = 6;
    private static final int RESULT_DEFAULT_REQUEST_USERAGENT = 7;
    private static final int RESULT_REQUEST_USERJS_SETTING = 8;
    private static final int RESULT_REQUEST_WEB_ENCODE_SETTING = 9;
    private static final int RESULT_REQUEST_SHARE_IMAGE = 10;
    private static final int RESULT_REQUEST_ACTION_LIST = 11;

    private static final String APPDATA_EXTRA_TARGET = "BrowserActivity.target";
    private static final String TAB_TYPE = "tabType";
    public static final String ACTION_FINISH = "jp.hazuki.yuzubrowser.BrowserActivity.finish";
    public static final String ACTION_NEW_TAB = "jp.hazuki.yuzubrowser.BrowserActivity.newTab";
    public static final String EXTRA_FORCE_DESTROY = "force_destroy";
    public static final String EXTRA_WINDOW_MODE = "window_mode";
    public static final String EXTRA_SHOULD_OPEN_IN_NEW_TAB = "shouldOpenInNewTab";

    private final MyActionCallback mActionCallback = new MyActionCallback();
    private final MyWebViewClient mWebViewClient = new MyWebViewClient();
    private final MyOnWebStateChangeListener mOnWebStateChangeListener = new MyOnWebStateChangeListener();
    private final MyOnCreateContextMenuListener mOnCreateContextMenuListener = new MyOnCreateContextMenuListener();
    private PermissionDialogHandler dialogHandler;
    private TabManager mTabManager;
    private Toolbar mToolbar;
    private PieControl mPieControl;
    private WebUploadHandler mWebUploadHandler;
    private WebCustomViewHandler mWebCustomViewHandler;
    private PatternUrlManager mPatternManager;
    private BrowserHistoryAsyncManager mBrowserHistoryManager;
    private SpeedDialAsyncManager mSpeedDialAsyncManager;
    private SpeedDialHtml speedDialHtml;
    private FaviconAsyncManager mFaviconAsyncManager;
    private HardButtonActionManager mHardButtonManager;
    private ArrayList<UserScript> mUserScriptList;
    private ArrayList<ResourceChecker> mResourceCheckerList;
    private AdBlockController adBlockController;
    private View mVideoLoadingProgressView;
    private boolean mIsFullScreenMode = false;
    private boolean mIsActivityPaused = true;
    private boolean mIsImeShown = false;
    private GestureManager mWebGestureManager;
    private WebViewFindDialog mWebViewFindDialog;
    private WebViewPageFastScroller mWebViewPageFastScroller;
    private WebViewAutoScrollManager mWebViewAutoScrollManager;
    private WebViewRenderingManager webViewRenderingManager = new WebViewRenderingManager();
    private ArrayDeque<Bundle> mClosedTabs;
    private MultiTouchGestureDetector mGestureDetector;
    private PointerView mCursorView;
    private Handler mHandler;
    private boolean mIsDestroyed;
    private boolean forceDestroy;

    private IntentFilter mNetworkStateChangedFilter;
    private BroadcastReceiver mNetworkStateBroadcastReceiver;
    private boolean mIsNetworkUp;

    private final Runnable mSaveTabsRunnable = new Runnable() {
        @Override
        public void run() {
            mTabManager.saveData();

            int delay = AppData.auto_tab_save_delay.get();
            if (delay > 0)
                mHandler.postDelayed(mSaveTabsRunnable, delay * 1000);
        }
    };

    private TabListLayout mTabManagerView;
    private FrameLayout webFrameLayout;
    private GestureFrameLayout webGestureOverlayView;
    private GestureOverlayView mSubGestureView;
    private RootLayout superFrameLayout;
    private CustomCoordinatorLayout coordinatorLayout;
    private WebViewFastScroller webViewFastScroller;
    private WebViewBehavior webViewBehavior;

    private MenuWindow menuWindow;

    private MultiFingerGestureManager multiFingerGestureManager;
    private MultiFingerGestureDetector multiFingerGestureDetector;
    private TextView actionNameTextView;
    private boolean isShowActionName;

    private boolean isResumed;
    private Action delayAction;

    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreenMode(AppData.fullscreen.get());

        setContentView(R.layout.browser_activity);

        if (BrowserApplication.isNeedLoad()) {
            AppData.load(this);
            BrowserApplication.setNeedLoad(false);
        }

        mHandler = new Handler();
        dialogHandler = new PermissionDialogHandler(this);
        mTabManager = TabManagerFactory.newInstance(this);

        WebViewProvider.setProvider(this);

        webFrameLayout = findViewById(R.id.webFrameLayout);
        webGestureOverlayView = findViewById(R.id.webGestureOverlayView);
        coordinatorLayout = findViewById(R.id.coordinator);
        webViewBehavior = (WebViewBehavior) ((CoordinatorLayout.LayoutParams) webGestureOverlayView.getLayoutParams()).getBehavior();
        superFrameLayout = findViewById(R.id.superFrameLayout);
        superFrameLayout.setOnImeShownListener(visible -> {
            if (mIsImeShown != visible) {
                mIsImeShown = visible;
                if (mToolbar != null) {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab != null)
                        mToolbar.notifyChangeWebState(tab);
                    mToolbar.onImeChanged(visible);
                }

                if (!visible && mIsFullScreenMode) {
                    getWindow().getDecorView()
                            .setSystemUiVisibility(DisplayUtils.getFullScreenVisibility());
                }
            }
        });

        findViewById(R.id.topToolbarLayout).getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    coordinatorLayout.setToolbarHeight(findViewById(R.id.topToolbarLayout).getHeight());
                    mTabManager.onLayoutCreated();
                });

        AppBarLayout appBarLayout = findViewById(R.id.appbar);

        webViewFastScroller = findViewById(R.id.webViewFastScroller);

        webViewFastScroller.attachAppBarLayout(coordinatorLayout, appBarLayout);

        webGestureOverlayView.setWebFrame(appBarLayout);

        final Context appContext = getApplicationContext();

        mHardButtonManager = HardButtonActionManager.getInstance(appContext);

        mPatternManager = new PatternUrlManager(appContext);

        mToolbar = new Toolbar();
        mToolbar.addToolbarView(getResources());

        mGestureDetector = new MultiTouchGestureDetector(appContext, new MyGestureListener());

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                mIsNetworkUp = networkInfo.isAvailable();
            }
        }

        mNetworkStateChangedFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkStateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
                    return;
                boolean networkUp = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (mIsNetworkUp != networkUp) {
                    for (TabData tab : mTabManager.getLoadedData()) {
                        tab.mWebView.setNetworkAvailable(networkUp);
                    }
                }
            }
        };

        mSpeedDialAsyncManager = new SpeedDialAsyncManager(getApplicationContext());
        speedDialHtml = new SpeedDialHtml(getApplicationContext());
        mFaviconAsyncManager = new FaviconAsyncManager(getApplicationContext());

        onPreferenceReset();
        mTabManager.setOnWebViewCreatedListener(this);

        if (savedInstanceState != null) {
            restoreWebState(savedInstanceState);
        } else {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeSessionCookies(null);

            mTabManager.loadData();
            if (mTabManager.size() > 0 && mTabManager.getCurrentTabNo() >= 0 && mTabManager.getCurrentTabNo() < mTabManager.size()) {
                setCurrentTab(mTabManager.getCurrentTabNo());
                mToolbar.scrollTabTo(mTabManager.getCurrentTabNo());
            }


            handleIntent(getIntent());
        }

        if (mTabManager.isEmpty()) {
            MainTabData first_tab = addNewTab(TabType.DEFAULT);
            setCurrentTab(0);
            loadUrl(first_tab, AppData.home_page.get());
        }
        MenuActionManager action_manager = MenuActionManager.getInstance(appContext);
        menuWindow = new MenuWindow(this, action_manager.browser_activity.list, mActionCallback);
        menuWindow.setListener(this);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> setFullscreenIfEnable());

        actionNameTextView = findViewById(R.id.actionNameTextView);
        webGestureOverlayView.setOnTouchListener((v, event) ->
                multiFingerGestureDetector != null && multiFingerGestureDetector.onTouchEvent(event));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (AppData.auto_tab_save_delay.get() > 0)
            mHandler.post(mSaveTabsRunnable);

        if (mIsActivityPaused) {
            mIsActivityPaused = false;
            MainTabData tab = mTabManager.getCurrentTabData();
            if (tab != null) {
                tab.mWebView.onResume();
                resumeWebViewTimers(tab);
            }
        } else {
            Logger.w(TAG, "Activity is already started");
        }


        WebViewUtils.enablePlatformNotifications();
        WebViewProxy.setProxy(getApplicationContext(), AppData.proxy_set.get(), AppData.proxy_address.get());

        registerReceiver(mNetworkStateBroadcastReceiver, mNetworkStateChangedFilter);

        MainTabData tabData = mTabManager.getCurrentTabData();
        if (tabData != null && tabData.mWebView.getView().getParent() == null) {
            setCurrentTab(getCurrentTab());
        }

        if (delayAction != null) {
            mActionCallback.run(delayAction);
            delayAction = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        dialogHandler.resume();
        setFullscreenIfEnable();
        mToolbar.resetToolBar();
        PermissionUtils.checkFirst(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
        dialogHandler.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTabManagerView != null)
            mTabManagerView.closeSnackBar();
        if (adBlockController != null)
            adBlockController.onResume();

        mTabManager.saveData();
        mHandler.removeCallbacks(mSaveTabsRunnable);
        FaviconManager.getInstance(getApplicationContext()).save();

        if (mIsActivityPaused) {
            Logger.w(TAG, "Activity is already stopped");
            return;
        }

        if (AppData.pause_web_background.get()) {
            mIsActivityPaused = true;

            MainTabData tab = mTabManager.getCurrentTabData();
            if (tab != null) {
                tab.mWebView.onPause();
                pauseWebViewTimers(tab);
            }
        }

        WebViewProxy.resetProxy(getApplicationContext());
        WebViewUtils.disablePlatformNotifications();

        unregisterReceiver(mNetworkStateBroadcastReceiver);
    }

    private boolean pauseWebViewTimers(MainTabData tab) {
        Logger.d(TAG, "pauseWebViewTimers");
        if (tab == null) return true;
        if (!tab.isInPageLoad()) {
            Logger.d(TAG, "pauseTimers");
            tab.mWebView.pauseTimers();
            return true;
        }
        return false;
    }

    private boolean resumeWebViewTimers(MainTabData tab) {
        Logger.d(TAG, "resumeWebViewTimers");
        if (tab == null) return true;
        boolean inLoad = tab.isInPageLoad();
        if ((!mIsActivityPaused && !inLoad) || (mIsActivityPaused && inLoad)) {
            Logger.d(TAG, "resumeTimers");
            tab.mWebView.resumeTimers();
            return true;
        }
        return false;
    }

    private void destroy() {
        if (mIsDestroyed)
            return;

        if (mWebCustomViewHandler != null) {
            mWebCustomViewHandler.hideCustomView(this);
            mWebCustomViewHandler = null;
        }
        if (mWebUploadHandler != null) {
            mWebUploadHandler.destroy();
            mWebUploadHandler = null;
        }
        webFrameLayout.removeAllViews();
        mTabManager.destroy();
        if (mBrowserHistoryManager != null) {
            mBrowserHistoryManager.destroy();
            mBrowserHistoryManager = null;
        }
        mSpeedDialAsyncManager.destroy();
        mFaviconAsyncManager.destroy();
        mIsDestroyed = true;
    }

    @Override
    protected void onDestroy() {
        Logger.d(TAG, "onDestroy()");
        super.onDestroy();
        destroy();
        if (AppData.kill_process.get() || forceDestroy)
            Process.killProcess(Process.myPid());
    }

    @Override
    public void onMenuClose() {
        setFullscreenIfEnable();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            setFullscreenIfEnable();
    }

    @Override
    protected boolean isLoadThemeData() {
        return true;
    }

    @Override
    protected int lightThemeResource() {
        return R.style.BrowserMinThemeLight_NoTitle;
    }

    private void setFullscreenIfEnable() {
        if (mIsFullScreenMode) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(DisplayUtils.getFullScreenVisibility());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        saveWebState(bundle);
    }

    @Override
    public boolean saveWebState(Bundle bundle) {
        if (mIsDestroyed)
            return false;
        if (mTabManagerView != null)
            mTabManagerView.closeSnackBar();
        mTabManager.saveData();
        return true;
    }

    @Override
    public void restoreWebState(Bundle bundle) {
        mTabManager.loadData();
        if (!mTabManager.isEmpty())
            mToolbar.scrollTabTo(mTabManager.getCurrentTabNo());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mToolbar != null)
            mToolbar.onActivityConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mVideoLoadingProgressView = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_REQUEST_WEB_UPLOAD:
                if (mWebUploadHandler != null)
                    mWebUploadHandler.onActivityResult(resultCode, data);
                break;
            case RESULT_REQUEST_SEARCHBOX: {
                if (resultCode != RESULT_OK || data == null) break;
                String query = data.getStringExtra(SearchActivity.EXTRA_QUERY);
                String searchUrl = data.getStringExtra(SearchActivity.EXTRA_SEARCH_URL);

                if (TextUtils.isEmpty(query)) break;

                String url;
                switch (data.getIntExtra(SearchActivity.EXTRA_SEARCH_MODE, SearchActivity.SEARCH_MODE_AUTO)) {
                    case SearchActivity.SEARCH_MODE_URL:
                        url = WebUtils.makeUrl(query);
                        break;
                    case SearchActivity.SEARCH_MODE_WORD:
                        url = WebUtils.makeSearchUrlFromQuery(query, searchUrl, "%s");
                        break;
                    //case SearchActivity.SEARCH_MODE_AUTO:
                    default:
                        url = WebUtils.makeUrlFromQuery(query, searchUrl, "%s");
                        break;
                }
                Bundle appdata = data.getBundleExtra(SearchActivity.EXTRA_APP_DATA);
                int target = appdata.getInt(APPDATA_EXTRA_TARGET, -1);
                MainTabData tab = mTabManager.get(target);
                if (data.getBooleanExtra(SearchActivity.EXTRA_OPEN_NEW_TAB, false) || tab == null)
                    openInNewTab(url, TabType.DEFAULT);
                else
                    loadUrl(tab, url);
            }
            break;
            case RESULT_REQUEST_BOOKMARK:
            case RESULT_REQUEST_HISTORY: {
                if (resultCode != RESULT_OK || data == null) break;
                BrowserOpenable openable = data.getParcelableExtra(BrowserManager.EXTRA_OPENABLE);
                if (openable == null)
                    break;
                openable.open(this);
            }
            break;
            case RESULT_REQUEST_SETTING:
                AppData.load(getApplicationContext());
                onPreferenceReset();
                break;
            case RESULT_REQUEST_USERAGENT: {
                if (resultCode != RESULT_OK || data == null) break;
                String ua = data.getStringExtra(Intent.EXTRA_TEXT);
                if (ua == null) return;
                MainTabData tab = mTabManager.getCurrentTabData();
                tab.mWebView.getSettings().setUserAgentString(ua);
                tab.mWebView.reload();
            }
            break;
            case RESULT_DEFAULT_REQUEST_USERAGENT: {
                if (resultCode != RESULT_OK || data == null) break;
                String ua = data.getStringExtra(Intent.EXTRA_TEXT);
                if (ua == null) return;
                AppData.user_agent.set(ua);
                AppData.commit(this, AppData.user_agent);
                for (MainTabData tabData : mTabManager.getLoadedData()) {
                    tabData.mWebView.getSettings().setUserAgentString(ua);
                    tabData.mWebView.reload();
                }
            }
            break;
            case RESULT_REQUEST_USERJS_SETTING:
                resetUserScript(AppData.userjs_enable.get());
                break;
            case RESULT_REQUEST_WEB_ENCODE_SETTING: {
                if (resultCode != RESULT_OK || data == null) break;
                String encoding = data.getStringExtra(Intent.EXTRA_TEXT);
                if (encoding == null) return;
                MainTabData tab = mTabManager.getCurrentTabData();
                tab.mWebView.getSettings().setDefaultTextEncodingName(encoding);
                tab.mWebView.reload();
            }
            break;
            case RESULT_REQUEST_SHARE_IMAGE:
                if (resultCode != RESULT_OK || data == null) break;
                Uri uri = data.getData();
                String mineType = data.getStringExtra(FastDownloadActivity.EXTRA_MINE_TYPE);

                Intent open = new Intent(Intent.ACTION_SEND);
                if (mineType != null)
                    open.setType(mineType);
                else
                    open.setType("image/*");

                open.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                open.putExtra(Intent.EXTRA_STREAM, uri);
                open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(open, getText(R.string.share)));
                break;
            case RESULT_REQUEST_ACTION_LIST:
                if (resultCode != RESULT_OK || data == null) break;
                Action action = data.getParcelableExtra(ActionActivity.EXTRA_ACTION);
                if (action == null) {
                    Logger.w(TAG, "Action is null");
                    break;
                }
                if (isResumed)
                    mActionCallback.run(action);
                else
                    delayAction = action;
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (AppData.volume_default_playing.get() && ((AudioManager) getSystemService(AUDIO_SERVICE)).isMusicActive()) {
                    return super.onKeyDown(keyCode, event);

                } else if (mWebCustomViewHandler == null || !mWebCustomViewHandler.isCustomViewShowing()) {
                    if (mActionCallback.run(mHardButtonManager.volume_up.action))
                        return true;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (AppData.volume_default_playing.get() && ((AudioManager) getSystemService(AUDIO_SERVICE)).isMusicActive()) {
                    return super.onKeyDown(keyCode, event);

                } else if (mWebCustomViewHandler == null || !mWebCustomViewHandler.isCustomViewShowing()) {
                    if (mActionCallback.run(mHardButtonManager.volume_down.action))
                        return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mWebCustomViewHandler == null || !mWebCustomViewHandler.isCustomViewShowing()) {
                    if (!mHardButtonManager.volume_up.action.isEmpty())
                        return true;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mWebCustomViewHandler == null || !mWebCustomViewHandler.isCustomViewShowing()) {
                    if (!mHardButtonManager.volume_down.action.isEmpty())
                        return true;
                }
                break;
            case KeyEvent.KEYCODE_CAMERA:
                if (!event.isCanceled() && mActionCallback.run(mHardButtonManager.camera_press.action)) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                if (!event.isCanceled()) {
                    if (menuWindow.isShowing()) {
                        menuWindow.dismiss();
                    } else {
                        menuWindow.show(findViewById(R.id.superFrameLayout), OpenOptionsMenuAction.getGravity(AppData.menu_btn_list_mode.get()));
                    }
                    return true;
                }
                break;

        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackKeyPressed() {
        if (menuWindow != null && menuWindow.isShowing()) {
            menuWindow.dismiss();
        } else if (mWebCustomViewHandler != null && mWebCustomViewHandler.isCustomViewShowing()) {
            mWebCustomViewHandler.hideCustomView(this);
        } else if (mSubGestureView != null) {
            superFrameLayout.removeView(mSubGestureView);
            mSubGestureView = null;
        } else if (mCursorView != null && mCursorView.getBackFinish()) {
            mCursorView.setView(null);
            webFrameLayout.removeView(mCursorView);
            mCursorView = null;
        } else if (mTabManagerView != null) {
            mTabManagerView.close();
        } else if (mWebViewFindDialog != null && mWebViewFindDialog.isVisible()) {
            mWebViewFindDialog.hide();
        } else if (mWebViewPageFastScroller != null) {
            mWebViewPageFastScroller.close();
        } else if (mWebViewAutoScrollManager != null) {
            mWebViewAutoScrollManager.stop();
        } else if (mTabManager.getCurrentTabData() != null
                && mTabManager.getCurrentTabData().mWebView.canGoBack()) {
            mTabManager.getCurrentTabData().mWebView.goBack();
            superFrameLayout.postDelayed(takeCurrentTabScreen, 500);
        } else {
            mActionCallback.run(mHardButtonManager.back_press.action);
        }
    }

    @Override
    public void onBackKeyLongPressed() {
        mActionCallback.run(mHardButtonManager.back_lpress.action);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH && !event.isCanceled()) {
            mActionCallback.run(mHardButtonManager.search_press.action);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void setFullScreenMode(boolean enable) {
        if (mIsFullScreenMode == enable) return;

        mIsFullScreenMode = enable;

        if (mToolbar != null) {
            mToolbar.onFullscreeenChanged(enable);
        }

        if (enable) {
            int visibility = DisplayUtils.getFullScreenVisibility();
            getWindow().getDecorView()
                    .setSystemUiVisibility(visibility);
            if (menuWindow != null)
                menuWindow.setSystemUiVisibility(visibility);
            if (DisplayUtils.isNeedFullScreenFlag())
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            int flag = ThemeData.getSystemUiVisibilityFlag();
            getWindow().getDecorView()
                    .setSystemUiVisibility(flag);
            if (menuWindow != null)
                menuWindow.setSystemUiVisibility(flag);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private boolean handleIntent(Intent intent) {

        if (intent == null) return false;
        String action = intent.getAction();
        setIntent(new Intent());
        if (action == null) return false;
        if (ACTION_FINISH.equals(action)) {
            forceDestroy = intent.getBooleanExtra(EXTRA_FORCE_DESTROY, false);
            BrowserApplication.setNeedLoad(true);
            finish();
            return false;
        }
        if (ACTION_NEW_TAB.equals(action)) {
            openInNewTab(AppData.home_page.get(), TabType.DEFAULT);
            return false;
        }
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return false;
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            String url = intent.getDataString();
            boolean window = intent.getBooleanExtra(EXTRA_WINDOW_MODE, false);
            if (TextUtils.isEmpty(url))
                url = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(url))
                openInNewTab(url, window ? TabType.WINDOW : TabType.INTENT, intent.getBooleanExtra(EXTRA_SHOULD_OPEN_IN_NEW_TAB, false));
            else {
                Logger.w(TAG, "ACTION_VIEW : url is null or empty.");
                return false;
            }
        } else if (Constants.intent.ACTION_OPEN_DEFAULT.equals(action)) {
            openInNewTab(intent.getDataString(), TabType.DEFAULT);
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings("WrongConstant")
    private void onPreferenceReset() {
        mToolbar.onPreferenceReset();
        mTabManager.onPreferenceReset();
        webViewRenderingManager.onPreferenceReset();
        mPatternManager.load(getApplicationContext());

        if (ThemeData.createInstance(getApplicationContext(), AppData.theme_setting.get()) != null) {
            ThemeData themedata = ThemeData.getInstance();
            mToolbar.onThemeChanged(themedata);
            if (mPieControl != null)
                mPieControl.onThemeChanged(themedata);
            mToolbar.notifyChangeWebState();

            if (themedata.statusBarColor != 0) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(themedata.statusBarColor);
                window.getDecorView().setSystemUiVisibility(ThemeData.getSystemUiVisibilityFlag());
            }

            if (themedata.scrollbarAccentColor != 0) {
                webViewFastScroller.setHandlePressedColor(themedata.scrollbarAccentColor);
            } else if (themedata.tabAccentColor != 0) {
                webViewFastScroller.setHandlePressedColor(themedata.tabAccentColor);
            }
        }

        if (!AppData.private_mode.get() && AppData.save_history.get()) {
            if (mBrowserHistoryManager == null)
                mBrowserHistoryManager = new BrowserHistoryAsyncManager(getApplicationContext());
        } else {
            if (mBrowserHistoryManager != null)
                mBrowserHistoryManager.destroy();
            mBrowserHistoryManager = null;
        }

        if (AppData.resblock_enable.get())
            mResourceCheckerList = new ResourceBlockManager(getApplicationContext()).getList();
        else
            mResourceCheckerList = null;

        if (AppData.ad_block.get())
            adBlockController = new AdBlockController(getApplicationContext());
        else
            adBlockController = null;

        for (MainTabData tabdata : mTabManager.getLoadedData()) {
            initWebSetting(tabdata.mWebView);
            tabdata.mWebView.onPreferenceReset();
        }

        final MainTabData tab = mTabManager.getCurrentTabData();
        if (tab != null)
            mToolbar.notifyChangeWebState(tab);

        setQuickControlEnabled(AppData.qc_enable.get());

        setRequestedOrientation(AppData.oritentation.get());
        setFullScreenMode(AppData.fullscreen.get());

        if (AppData.keep_screen_on.get())
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        boolean cookie = AppData.private_mode.get()
                ? AppData.accept_cookie.get() && AppData.accept_cookie_private.get()
                : AppData.accept_cookie.get();

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(cookie);

        final boolean thirdCookie = cookie && AppData.accept_third_cookie.get();
        for (MainTabData tabData : mTabManager.getLoadedData()) {
            tabData.mWebView.setAcceptThirdPartyCookies(cookieManager, thirdCookie);
        }

        if (AppData.gesture_enable_web.get()) {
            mWebGestureManager = GestureManager.getInstance(getApplicationContext(), GestureManager.GESTURE_TYPE_WEB);
            mWebGestureManager.load();

            webGestureOverlayView.setEnabled(true);
            webGestureOverlayView.setGestureVisible(AppData.gesture_line_web.get());

            webGestureOverlayView.removeAllOnGestureListeners();
            webGestureOverlayView.removeAllOnGesturePerformedListeners();

            webGestureOverlayView.addOnGestureListener(this);
            webGestureOverlayView.addOnGesturePerformedListener(this);
        } else {
            if (mWebGestureManager != null)
                mWebGestureManager = null;

            webGestureOverlayView.removeAllOnGestureListeners();
            webGestureOverlayView.removeAllOnGesturePerformedListeners();
            webGestureOverlayView.setEnabled(false);
        }

        setMultiFingerGestureEnabled(AppData.multi_finger_gesture.get());

        resetUserScript(AppData.userjs_enable.get());

        int touchScrollbar = AppData.touch_scrollbar.get();
        if (touchScrollbar >= 0) {
            webViewFastScroller.setScrollEnabled(true);
            webViewFastScroller.setShowLeft(touchScrollbar == 1);
        } else {
            webViewFastScroller.setScrollEnabled(false);
        }


        MenuActionManager action_manager = MenuActionManager.getInstance(getApplicationContext());
        menuWindow = new MenuWindow(this, action_manager.browser_activity.list, mActionCallback);

        ErrorReport.setDetailedLog(AppData.detailed_log.get());
    }

    @Override
    public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
        if (mToolbar.isContainsWebToolbar(event))
            overlay.cancelGesture();
    }

    @Override
    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
    }

    @Override
    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
    }

    @Override
    public void onGesture(GestureOverlayView overlay, MotionEvent event) {
        if (event.getPointerCount() > 1)
            overlay.cancelGesture();//multiple touch is disabled
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        Action action = mWebGestureManager.recognize(gesture);
        if (action != null) {
            mActionCallback.run(action);
        }
    }

    private void resetUserScript(boolean enable) {
        if (enable) {
            mUserScriptList = new UserScriptDatabase(getApplicationContext()).getEnableJsDataList();
        } else {
            if (mUserScriptList != null)
                mUserScriptList = null;
        }
    }

    private void applyUserScript(CustomWebView web, String url, boolean isStart) {
        if (mUserScriptList != null) {
            SCRIPT_LOOP:
            for (UserScript script : mUserScriptList) {
                if (isStart != script.isRunStart())
                    continue;

                if (script.getExclude() != null)
                    for (Pattern pattern : script.getExclude()) {
                        if (pattern.matcher(url).find())
                            continue SCRIPT_LOOP;
                    }

                if (script.getInclude() != null)
                    for (Pattern pattern : script.getInclude()) {
                        if (pattern.matcher(url).find()) {
                            web.evaluateJavascript(script.getRunnable(), null);

                            continue SCRIPT_LOOP;
                        }
                    }
            }
        }
    }

    private void setMultiFingerGestureEnabled(boolean enable) {
        if (enable) {
            multiFingerGestureManager = new MultiFingerGestureManager(this);
            if (multiFingerGestureDetector == null)
                multiFingerGestureDetector = new MultiFingerGestureDetector(this, new MyMfGestureListener());
            multiFingerGestureDetector.setShowName(AppData.multi_finger_gesture_show_name.get());
            multiFingerGestureDetector.setSensitivity(AppData.multi_finger_gesture_sensitivity.get());
        } else {
            if (multiFingerGestureDetector != null)
                multiFingerGestureDetector = null;

            if (multiFingerGestureManager != null)
                multiFingerGestureManager = null;
        }
    }

    private boolean getMultiFingerGestureEnabled() {
        return multiFingerGestureManager != null;
    }

    private void setQuickControlEnabled(boolean enable) {
        if (enable) {
            if (mPieControl == null) {
                mPieControl = new PieControl(this, mActionCallback);
                mPieControl.attachToLayout(webFrameLayout);
            }
            mPieControl.onPreferenceReset();
            mPieControl.onThemeChanged(ThemeData.getInstance());
        } else {
            if (mPieControl != null) {
                mPieControl.detachFromLayout(webFrameLayout);
                mPieControl = null;
            }
        }
    }

    private boolean getQuickControlEnabled() {
        return mPieControl != null;
    }

    private int checkPatternMatch(MainTabData tab, String url, boolean shouldOpenInNewTab) {
        if (url == null) return -1;
        boolean normalSettings = true;
        boolean changeSetting = false;
        for (PatternUrlChecker pattern : mPatternManager.getList()) {
            if (!pattern.isMatchUrl(url)) continue;
            if (pattern.getAction() instanceof WebSettingPatternAction) {
                if (tab.getResetAction() != null && tab.getResetAction().getPatternAction() == pattern.getAction()) {
                    normalSettings = false;
                    continue;
                }

                /* save normal settings */
                if (tab.getResetAction() == null)
                    tab.setResetAction(new WebSettingResetAction(tab));
                tab.getResetAction().setPatternAction((WebSettingPatternAction) pattern.getAction());

                /* change web settings */
                pattern.getAction().run(this, tab, url);
                changeSetting = true;
            } else if (shouldOpenInNewTab && pattern.getAction() instanceof OpenOthersPatternAction) {
                continue;
            } else if (pattern.getAction().run(this, tab, url))
                return 1;
        }

        if (changeSetting) {
            return 0;
        }

        /* reset to normal */
        if (normalSettings && tab.getResetAction() != null) {
            tab.getResetAction().reset(tab);
            tab.setResetAction(null);
            mToolbar.notifyChangeWebState();
            return 0;
        }
        return -1;
    }

    private boolean checkNewTabLink(int perform, WebViewTransport transport) {
        switch (perform) {
            case BrowserManager.LOAD_URL_TAB_NEW:
                openInNewTab(transport);
                return true;
            case BrowserManager.LOAD_URL_TAB_BG:
                openInBackground(transport);
                return true;
            case BrowserManager.LOAD_URL_TAB_NEW_RIGHT:
                openInRightNewTab(transport);
                return true;
            case BrowserManager.LOAD_URL_TAB_BG_RIGHT:
                openInRightBgTab(transport);
                return true;
            default:
                throw new IllegalArgumentException("Unknown perform:" + perform);
        }
    }

    private boolean checkNewTabLinkUser(int perform, String url, @TabType int type) {
        if (perform < 0)
            return false;

        if (perform == BrowserManager.LOAD_URL_TAB_CURRENT)
            return false;

        return !WebViewUtils.shouldLoadSameTabUser(url) && performNewTabLink(perform, null, url, type);

    }

    private int getNewTabPerformType(MainTabData tab) {
        if (UrlUtils.isSpeedDial(tab.getOriginalUrl())) {
            return AppData.newtab_speeddial.get();
        } else {
            return AppData.newtab_link.get();
        }
    }

    private boolean checkNewTabLinkAuto(int perform, MainTabData tab, String url) {
        if (tab.isNavLock() && !WebViewUtils.shouldLoadSameTabAuto(url)) {
            performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, url, TabType.WINDOW);
            return true;
        }

        if (perform == BrowserManager.LOAD_URL_TAB_CURRENT)
            return false;

        if (WebViewUtils.shouldLoadSameTabAuto(url))
            return false;

        if (WebViewUtils.shouldLoadSameTabScheme(url))
            return false;

        return !(TextUtils.equals(url, tab.getUrl()) || tab.mWebView.isBackForwardListEmpty()) && performNewTabLink(perform, tab, url, TabType.WINDOW);

    }

    private boolean performNewTabLink(int perform, MainTabData tab, String url, @TabType int type) {
        switch (perform) {
            case BrowserManager.LOAD_URL_TAB_CURRENT:
                loadUrl(tab, url);
                return true;
            case BrowserManager.LOAD_URL_TAB_NEW:
                openInNewTab(url, type);
                return true;
            case BrowserManager.LOAD_URL_TAB_BG:
                openInBackground(url, type);
                return true;
            case BrowserManager.LOAD_URL_TAB_NEW_RIGHT:
                openInRightNewTab(url, type);
                return true;
            case BrowserManager.LOAD_URL_TAB_BG_RIGHT:
                openInRightBgTab(url, type);
                return true;
            default:
                throw new IllegalArgumentException("Unknown perform:" + perform);
        }
    }

    @Override
    public CustomWebView makeWebView(@WebViewType int cacheType) {
        CustomWebView web = WebViewFactory.create(this, cacheType);
        web.getView().setId(View.generateViewId());
        web.getWebView().setFocusableInTouchMode(true);
        web.getWebView().setFocusable(true);
        web.getWebView().setDrawingCacheEnabled(false);
        web.getWebView().setWillNotCacheDrawing(true);
        initWebSetting(web);
        boolean enable_cookie = AppData.private_mode.get()
                ? AppData.accept_cookie.get() && AppData.accept_cookie_private.get()
                : AppData.accept_cookie.get();
        web.setAcceptThirdPartyCookies(CookieManager.getInstance(), enable_cookie && AppData.accept_third_cookie.get());
        return web;
    }

    @Override
    public MainTabData addNewTab(@TabType int type) {
        return addNewTab(WebViewFactory.getMode(), type);
    }

    @Override
    public MainTabData addNewTab(@WebViewType int cacheType, @TabType int type) {
        CustomWebView web = makeWebView(cacheType);
// TODO: Restore this when Google fixes the bug where the WebView is blank after calling onPause followed by onResume.
//        if (AppData.pause_web_tab_change.get())
//            web.onPause();
        MainTabData tabdata = mTabManager.add(web, mToolbar.addNewTabView());
        tabdata.setTabType(type);
        if (type == TabType.WINDOW) {
            MainTabData nowData = mTabManager.get(mTabManager.getCurrentTabNo());
            if (nowData != null)
                tabdata.setParent(nowData.mWebView.getIdentityId());
        }
        if (ThemeData.isEnabled())
            tabdata.onMoveTabToBackground(getResources(), getTheme());

        mToolbar.notifyChangeWebState();
        return tabdata;
    }

    private MainTabData openNewTab(@TabType int type) {
        MainTabData tab_data = addNewTab(type);
        setCurrentTab(mTabManager.getLastTabNo());
        mToolbar.scrollTabRight();
        return tab_data;
    }

    private MainTabData openNewTab(@WebViewType int cacheType, @TabType int type) {
        MainTabData tab_data = addNewTab(cacheType, type);
        setCurrentTab(mTabManager.getLastTabNo());
        mToolbar.scrollTabRight();
        return tab_data;
    }

    @Override
    public void openInNewTab(String url, @TabType int type) {
        loadUrl(openNewTab(type), url);
    }

    public void openInNewTab(String url, @TabType int type, boolean shouldOpenInNewTab) {
        loadUrl(openNewTab(type), url, shouldOpenInNewTab);
    }

    private void openInNewTab(WebViewTransport web_transport) {
        web_transport.setWebView(openNewTab(TabType.WINDOW).mWebView.getWebView());
    }

    @SuppressWarnings("WrongConstant")
    private void openInNewTab(Bundle state) {
        openNewTab(WebViewFactory.getMode(state), state.getInt(TAB_TYPE, 0)).mWebView.restoreState(state);
    }

    private void openInNewTab(MainTabData data) {
        Bundle bundle = new Bundle();
        data.mWebView.saveState(bundle);
        MainTabData newTab = openNewTab(WebViewFactory.getMode(bundle), bundle.getInt(TAB_TYPE, data.getTabType()));
        newTab.mWebView.restoreState(bundle);
        newTab.mWebView.setIdentityId(newTab.getId());
        newTab.setUrl(data.getUrl());
        newTab.setTitle(data.getTitle());
        newTab.setParent(newTab.getParent());
    }

    private void openInNewTabPost(final String url, @TabType int type) {
        openInNewTab(url, type);
    }

    @Override
    public void openInBackground(String url, @TabType int type) {
        MainTabData data = addNewTab(type);
        data.setUpBgTab();
        loadUrl(data, url);
    }

    private void openInBackground(WebViewTransport web_transport) {
        MainTabData data = addNewTab(TabType.WINDOW);
        data.setUpBgTab();
        web_transport.setWebView(data.mWebView.getWebView());
    }

    private MainTabData openRightNewTab(@TabType int type) {
        MainTabData tab_data = addNewTab(type);
        int from = mTabManager.getLastTabNo();
        int to = mTabManager.getCurrentTabNo() + 1;
        setCurrentTab(from);
        if (!moveTab(from, to))//maybe already right-most
            mToolbar.scrollTabRight();
        return tab_data;
    }

    @Override
    public void openInRightNewTab(String url, @TabType int type) {
        loadUrl(openRightNewTab(type), url);
    }

    private void openInRightNewTab(WebViewTransport web_transport) {
        WebView webView = openRightNewTab(TabType.WINDOW).mWebView.getWebView();
        web_transport.setWebView(webView);
    }

    private void openInRightNewTabPost(final String url, @TabType int type) {
        openInRightNewTab(url, type);
    }

    private MainTabData openRightBgTab(@TabType int type) {
        MainTabData tab_data = addNewTab(type);
        int from = mTabManager.getLastTabNo();
        int to = mTabManager.getCurrentTabNo() + 1;
        moveTab(from, to);
        tab_data.setUpBgTab();
        return tab_data;
    }

    @Override
    public void openInRightBgTab(String url, @TabType int type) {
        loadUrl(openRightBgTab(type), url);
    }

    private void openInRightBgTab(WebViewTransport web_transport) {
        web_transport.setWebView(openRightBgTab(TabType.WINDOW).mWebView.getWebView());
    }

    private void loadUrl(MainTabData tab, String url) {
        loadUrl(tab, url, false);
    }

    private void loadUrl(MainTabData tab, String url, boolean shouldOpenInNewTab) {
        if (tab.isNavLock() && !WebViewUtils.shouldLoadSameTabUser(url)) {
            performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, url, TabType.WINDOW);
            return;
        }
        if (AppData.file_access.get() == PreferenceConstants.FILE_ACCESS_SAFER && URLUtil.isFileUrl(url))
            url = SafeFileProvider.convertToSaferUrl(url);
        if (!preload(tab, url, Uri.parse(url))) {
            if (checkPatternMatch(tab, url, shouldOpenInNewTab) <= 0)
                tab.mWebView.loadUrl(url);
        }
    }

    @Override
    public void loadUrl(String url, int target) {
        loadUrl(mTabManager.getCurrentTabData(), url, target, TabType.WINDOW);
    }

    @Override
    public ToolbarManager getToolbar() {
        return mToolbar;
    }

    private void loadUrl(MainTabData tab, String url, int target, @TabType int type) {
        if (!checkNewTabLinkUser(target, url, type))
            loadUrl(tab, url);
    }

    private boolean preload(MainTabData data, String url, Uri uri) {
        String scheme = uri.getScheme();

        if (scheme != null) {
            scheme = scheme.toLowerCase();
            if (scheme.equals("intent")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                    if (intent != null) {
                        if (intent.getComponent() != null && BookmarkActivity.class.getName().equals(intent.getComponent().getClassName())) {
                            startActivityForResult(intent, RESULT_REQUEST_BOOKMARK);
                        } else {
                            PackageManager packageManager = getPackageManager();
                            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                            if (info != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                try {
                                    startActivity(intent);
                                    return true;
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                            }
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            if (!TextUtils.isEmpty(fallbackUrl)) {
                                loadUrl(data, fallbackUrl);
                            }
                        }
                        return true;
                    }
                } catch (URISyntaxException e) {
                    Logger.e(TAG, "Can't resolve intent://", e);
                }
            }

            if (scheme.equals("yuzu")) {

                String action = uri.getSchemeSpecificPart();

                Intent intent;
                if (TextUtils.isEmpty(action)) {
                    return false;
                } else switch (action.toLowerCase()) {
                    case "settings":
                    case "setting":
                        intent = new Intent(BrowserActivity.this, MainSettingsActivity.class);
                        break;
                    case "histories":
                    case "history":
                        intent = new Intent(BrowserActivity.this, BrowserHistoryActivity.class);
                        intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag());
                        intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, getRequestedOrientation());
                        startActivityForResult(intent, RESULT_REQUEST_HISTORY);
                        return true;
                    case "downloads":
                    case "download":
                        intent = new Intent(BrowserActivity.this, DownloadListActivity.class);
                        intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag());
                        intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, getRequestedOrientation());
                        break;
                    case "debug":
                        intent = new Intent(BrowserActivity.this, DebugActivity.class);
                        break;
                    case "bookmarks":
                    case "bookmark":
                        intent = new Intent(BrowserActivity.this, BookmarkActivity.class);
                        intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag());
                        intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, getRequestedOrientation());
                        startActivityForResult(intent, RESULT_REQUEST_BOOKMARK);
                        return true;
                    case "search":
                        showSearchBox("", mTabManager.indexOf(data.getId()), false, "reverse".equalsIgnoreCase(uri.getFragment()));
                        return true;
                    case "speeddial":
                        return false;
                    case "home":
                        if ("yuzu:home".equalsIgnoreCase(AppData.home_page.get()) || "yuzu://home".equalsIgnoreCase(AppData.home_page.get())) {
                            AppData.home_page.set("about:blank");
                            AppData.commit(this, AppData.home_page);
                        }
                        loadUrl(data, AppData.home_page.get());
                        return true;
                    case "resblock":
                        intent = new Intent(BrowserActivity.this, ResourceBlockListActivity.class);
                        break;
                    case "adblock":
                        intent = new Intent(BrowserActivity.this, AdBlockActivity.class);
                        break;
                    default:
                        return false;
                }
                startActivity(intent);
                return true;
            }

            if (AppData.share_unknown_scheme.get()) {
                if (WebUtils.isOverrideScheme(uri)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    ResolveInfo info = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (info != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                            return true;
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    if (!TextUtils.isEmpty(fallbackUrl)) {
                        loadUrl(data, fallbackUrl);
                    }
                    return true;
                }
            }
        }


        return false;
    }

    @Override
    public void setCurrentTab(int no) {
        MainTabData old_data = mTabManager.getCurrentTabData();
        MainTabData new_data = mTabManager.get(no);

        mTabManager.setCurrentTab(no);
        mToolbar.changeCurrentTab(no, old_data, new_data);

        mTabManager.saveData();

        if (mWebViewFindDialog != null && mWebViewFindDialog.isVisible())
            mWebViewFindDialog.hide();

        if (mWebViewPageFastScroller != null)
            mWebViewPageFastScroller.close();

        if (mWebViewAutoScrollManager != null)
            mWebViewAutoScrollManager.stop();

        if (old_data != null) {
            old_data.mWebView.setOnMyCreateContextMenuListener(null);
            old_data.mWebView.setGestureDetector(null);
            webFrameLayout.removeView(old_data.mWebView.getView());
            webViewBehavior.setWebView(null);
            webViewFastScroller.detachWebView();

//TODO: Restore this when Google fixes the bug where the WebView is blank after calling onPause followed by onResume.
//            if (AppData.pause_web_tab_change.get())
//                old_data.mWebView.onPause();
        }

        CustomWebView new_web = new_data.mWebView;
        new_web.resumeTimers();
        new_web.onResume();
        if (new_web.getView().getParent() != null) {
            ((ViewGroup) new_web.getView().getParent()).removeView(new_web.getView());
        }
        webFrameLayout.addView(new_web.getView(), 0);
        webViewBehavior.setWebView(new_web);
        webViewFastScroller.attachWebView(new_web);

        new_web.setOnMyCreateContextMenuListener(mOnCreateContextMenuListener);
        new_web.setGestureDetector(mGestureDetector);

        if (mCursorView != null)
            mCursorView.setView(new_web.getView());

        if (!new_web.hasFocus())
            new_web.requestFocus();
    }

    @Override
    public int getCurrentTab() {
        return mTabManager.getCurrentTabNo();
    }

    @Override
    public int getTabCount() {
        return mTabManager.size();
    }

    private boolean removeTab(int no, boolean error) {
        return removeTab(no, error, true);
    }

    public boolean removeTab(int no, boolean error, boolean destroy) {
        if (mTabManager.size() <= 1) {
            //Last tab
            return false;
        }

        MainTabData old_data = mTabManager.get(no);

        if (old_data.isPinning()) {
            if (error)
                Toast.makeText(getApplicationContext(), R.string.pinned_tab_warning, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (mTabManager.getCurrentTabNo() == no) {
            setCurrentTab(getNewTabNo(no, old_data));
        }

        CustomWebView old_web = old_data.mWebView;

        if (AppData.save_closed_tab.get()) {
            Bundle outState = new Bundle();
            old_web.saveState(outState);
            outState.putInt(TAB_TYPE, old_data.getTabType());
            if (mClosedTabs == null)
                mClosedTabs = new ArrayDeque<>();
            mClosedTabs.push(outState);
        }

        old_web.setEmbeddedTitleBarMethod(null);

        mTabManager.remove(no);
        mToolbar.removeTab(no);
        mToolbar.notifyChangeWebState();

        if (destroy)
            old_web.destroy();
        return true;
    }

    private void addTab(int index, MainTabData tabData) {
        int current = mTabManager.getCurrentTabNo();
        mTabManager.addTab(index, tabData);
        mToolbar.addTab(index, tabData.getTabView());
        if (current != mTabManager.getCurrentTabNo()) {
            mToolbar.moveCurrentTabPosition(mTabManager.getCurrentTabNo());
        }
        if (AppData.save_closed_tab.get()) {
            mClosedTabs.poll();
        }
    }

    private int getNewTabNo(int no, MainTabData old_data) {
        if (AppData.move_to_parent.get() && old_data.getTabType() == TabType.WINDOW && old_data.getParent() != 0) {
            int new_no = mTabManager.searchParentTabNo(old_data.getParent());
            if (new_no >= 0) {
                return new_no;
            }
        }
        if (no == mTabManager.getLastTabNo()) {
            return no - 1;
        } else {
            return no + 1;
        }
    }

    private boolean removeTab(int no) {
        return removeTab(no, true);
    }

    private boolean moveTab(int from, int to) {
        if (from == to)
            return false;

        int current = mTabManager.getCurrentTabNo();
        int new_curernt = mTabManager.move(from, to);
        mToolbar.moveTab(from, to, new_curernt);
        if (current == from)
            mToolbar.scrollTabTo(to);
        return true;
    }

    private void swapTab(int a, int b) {
        if (a == b)
            return;
        int old_current_tab = mTabManager.getCurrentTabNo();
        int new_current_tab = (a == old_current_tab) ? b : (b == old_current_tab) ? a : -1;

        if (new_current_tab >= 0) {
            mTabManager.setCurrentTab(new_current_tab);
        }
        mTabManager.swap(a, b);
        mToolbar.swapTab(a, b);
    }

    @Override
    public void onAdBlockListUpdate() {
        if (adBlockController != null)
            adBlockController.update();
    }

    @Override
    public void onFinishPositiveButtonClicked(int clearTabNo, int newSetting) {
        finishQuick(clearTabNo, newSetting);//TODO dialog memory leak
    }

    @Override
    public void onFinishNeutralButtonClicked(int clearTabNo, int newSetting) {
        if (clearTabNo >= 0 && mTabManager.size() >= 2)
            removeTab(clearTabNo);
        moveTaskToBack(true);
    }

    @Override
    public void onWebViewCreated(MainTabData tab) {
        checkPatternMatch(tab, tab.getUrl(), true);
    }

    @Override
    public void requestIconChange() {
        mToolbar.notifyChangeWebState();
    }

    //Cache web view for performance reasons
    private NormalWebView cachedWebView;
    private boolean waitingForWebViewCache;

    @NotNull
    @Override
    public NormalWebView getWebView() {
        NormalWebView cache = cachedWebView;
        if (cache != null) {
            cachedWebView = null;
            createWebViewCache();
            return cache;
        }
        createWebViewCache();
        return new NormalWebView(this);
    }

    private void createWebViewCache() {
        if (waitingForWebViewCache) return;

        waitingForWebViewCache = true;
        mHandler.postDelayed(() -> {
            cachedWebView = new NormalWebView(this);
            waitingForWebViewCache = false;
        }, 100);
    }

    private final class MyGestureListener implements MultiTouchGestureDetector.OnMultiTouchGestureListener, MultiTouchGestureDetector.OnMultiTouchDoubleTapListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1 == null || e2 == null)
                return false;

            MainTabData tab = mTabManager.getCurrentTabData();
            if (tab != null) {
                mToolbar.onWebViewScroll(tab.mWebView, e1, e2, distanceX, distanceY);
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null)
                return false;

            MainTabData tab = mTabManager.getCurrentTabData();
            if (tab == null)
                return false;

            if (e1.getPointerCount() <= 1 || e2.getPointerCount() <= 1) {
                if (!AppData.flick_enable.get())
                    return false;

                if (AppData.flick_disable_scroll.get() && tab.isMoved())
                    return false;

                final float dx = Math.abs(velocityX);
                final float dy = Math.abs(velocityY);
                final float dist = e2.getX() - e1.getX();

                if (dy > dx)
                    return false;
                if (dx < AppData.flick_sensitivity_speed.get() * 100)
                    return false;
                if (Math.abs(dist) < AppData.flick_sensitivity_distance.get() * 10)
                    return false;
                if (e2.getEventTime() - e1.getEventTime() > 300L)
                    return false;

                if (AppData.flick_edge.get()) {
                    float x = e1.getX();
                    int slop = (int) getResources().getDimension(R.dimen.flick_slop);

                    if (x <= tab.mWebView.getView().getWidth() - slop && x >= slop) {
                        return false;
                    }
                }

                FlickActionManager manager = FlickActionManager.getInstance(getApplicationContext());

                if (dist < 0)
                    mActionCallback.run(manager.flick_left.action);
                else
                    mActionCallback.run(manager.flick_right.action);
            } else {
                if (!AppData.webswipe_enable.get())
                    return false;

                WebSwipeActionManager manager = WebSwipeActionManager.getInstance(getApplicationContext());

                final float distX0 = e2.getX(0) - e1.getX(0);
                final float distX1 = e2.getX(1) - e1.getX(1);
                final float distY0 = e2.getY(0) - e1.getY(0);
                final float distY1 = e2.getY(1) - e1.getY(1);

                int sense_speed = AppData.webswipe_sensitivity_speed.get() * 100;
                int sense_dist = AppData.webswipe_sensitivity_distance.get() * 10;

                if (checkWebSwipe(sense_speed, sense_dist, velocityX, distX0, distX1)) {
                    if (checkWebSwipe(sense_speed, sense_dist, velocityY, distY0, distY1))
                        return false;
                    if (distX0 < 0)
                        mActionCallback.run(manager.double_left.action);
                    else
                        mActionCallback.run(manager.double_right.action);
                    return true;
                }

                if (checkWebSwipe(sense_speed, sense_dist, velocityY, distY0, distY1)) {
                    if (checkWebSwipe(sense_speed, sense_dist, velocityX, distX0, distX1))
                        return false;
                    if (distY0 < 0)
                        mActionCallback.run(manager.double_up.action);
                    else
                        mActionCallback.run(manager.double_down.action);
                    return true;
                }
            }

            return false;
        }

        private boolean checkWebSwipe(int sense_speed, int sense_dist, float velocity, float dist0, float dist1) {
            return Math.abs(velocity) >= sense_speed && MathUtils.equalsSign(dist0, dist1) && Math.abs(dist0) >= sense_dist && Math.abs(dist1) >= sense_dist;
        }

        @Override
        public void onUp(MotionEvent e) {
            mToolbar.onWebViewTapUp();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (mWebViewAutoScrollManager != null)
                mWebViewAutoScrollManager.stop();

            MainTabData tabData = mTabManager.getCurrentTabData();

            if (tabData == null) return false;

            tabData.onDown();

            mTabManager.takeThumbnailIfNeeded(tabData);
            return false;
        }

        @Override
        public boolean onPointerDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onPointerUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e == null)
                return false;
            MainTabData tab = mTabManager.getCurrentTabData();
            if (tab == null)
                return false;

            tab.mWebView.setDoubleTapFling(e.getPointerCount() == 1);
            return false;
        }

        @Override
        public boolean onDoubleTapScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public boolean onDoubleTapFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!AppData.double_tap_flick_enable.get())
                return false;

            if (e1 == null || e2 == null)
                return false;

            if (e2.getEventTime() - e1.getEventTime() > 300L)
                return false;

            MainTabData tab = mTabManager.getCurrentTabData();
            if (tab == null)
                return false;

            if (e1.getPointerCount() <= 1 || e2.getPointerCount() <= 1) {

                final float dx = Math.abs(velocityX);
                final float dy = Math.abs(velocityY);

                final float distX = e2.getX() - e1.getX();
                final float distY = e2.getY() - e1.getY();

                DoubleTapFlickActionManager manager = DoubleTapFlickActionManager.getInstance(getApplicationContext());

                boolean returnValue;
                if (dy > dx) {
                    if (dy < AppData.double_tap_flick_sensitivity_speed.get() * 100)
                        return false;
                    if (Math.abs(distY) < AppData.double_tap_flick_sensitivity_distance.get() * 10)
                        return false;

                    if (distY < 0)
                        returnValue = mActionCallback.run(manager.flick_up.action);
                    else
                        returnValue = mActionCallback.run(manager.flick_down.action);
                } else {
                    if (dx < AppData.double_tap_flick_sensitivity_speed.get() * 100)
                        return false;
                    if (Math.abs(distX) < AppData.double_tap_flick_sensitivity_distance.get() * 10)
                        return false;

                    if (distX < 0)
                        returnValue = mActionCallback.run(manager.flick_left.action);
                    else
                        returnValue = mActionCallback.run(manager.flick_right.action);
                }
                return returnValue;

            }
            return false;
        }
    }

    private final class MyMfGestureListener implements MultiFingerGestureDetector.OnMultiFingerGestureListener {

        private final ActionNameArray nameArray = new ActionNameArray(BrowserActivity.this);

        @Override
        public boolean onGesturePerformed(MultiFingerGestureInfo info) {
            for (MultiFingerGestureItem item : multiFingerGestureManager.getGestureItems()) {
                if (info.match(item)) {
                    mActionCallback.run(item.getAction());
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onShowGestureName(MultiFingerGestureInfo info) {
            for (MultiFingerGestureItem item : multiFingerGestureManager.getGestureItems()) {
                if (info.match(item)) {
                    actionNameTextView.setVisibility(View.VISIBLE);
                    actionNameTextView.setText(item.getAction().toString(nameArray));
                    isShowActionName = true;
                    return;
                }
            }

            if (isShowActionName) {
                actionNameTextView.setVisibility(View.GONE);
                isShowActionName = false;
            }
        }

        @Override
        public void onDismissGestureName() {
            if (isShowActionName)
                actionNameTextView.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("WrongConstant")
    private void initWebSetting(final CustomWebView web) {
        web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        web.setOverScrollModeMethod(View.OVER_SCROLL_NEVER);

        webViewRenderingManager.setWebViewRendering(web);

        web.setMyWebChromeClient(new MyWebChromeClient());
        web.setMyWebViewClient(mWebViewClient);

        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
                switch (AppData.download_action.get()) {
                    case PreferenceConstants.DOWNLOAD_DO_NOTHING:
                        break;
                    case PreferenceConstants.DOWNLOAD_AUTO:
                        if (WebDownloadUtils.shouldOpen(contentDisposition)) {
                            actionOpen(url, userAgent, contentDisposition, mimetype, contentLength);
                        } else {
                            actionDownload(url, userAgent, contentDisposition, mimetype, contentLength);
                        }
                        break;
                    case PreferenceConstants.DOWNLOAD_DOWNLOAD:
                        actionDownload(url, userAgent, contentDisposition, mimetype, contentLength);
                        break;
                    case PreferenceConstants.DOWNLOAD_OPEN:
                        actionOpen(url, userAgent, contentDisposition, mimetype, contentLength);
                        break;
                    case PreferenceConstants.DOWNLOAD_SHARE:
                        actionShare(url);
                        break;
                    case PreferenceConstants.DOWNLOAD_SELECT: {

                        new AlertDialog.Builder(BrowserActivity.this)
                                .setTitle(R.string.download)
                                .setItems(
                                        new String[]{getString(R.string.download), getString(R.string.open), getString(R.string.share)},
                                        (dialog, which) -> {
                                            switch (which) {
                                                case 0:
                                                    actionDownload(url, userAgent, contentDisposition, mimetype, contentLength);
                                                    break;
                                                case 1:
                                                    actionOpen(url, userAgent, contentDisposition, mimetype, contentLength);
                                                    break;
                                                case 2:
                                                    actionShare(url);
                                                    break;
                                            }
                                        })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                    }
                    break;
                }

                if (web.isBackForwardListEmpty()) {
                    removeTab(mTabManager.indexOf(web.getIdentityId()));
                }
            }

            private void actionDownload(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadDialog.showDownloadDialog(BrowserActivity.this, url, userAgent, contentDisposition, mimetype, contentLength, null);
            }

            private void actionOpen(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (!WebDownloadUtils.openFile(BrowserActivity.this, url, mimetype)) {
                    //application not found
                    Toast.makeText(getApplicationContext(), R.string.app_notfound, Toast.LENGTH_SHORT).show();
                    actionDownload(url, userAgent, contentDisposition, mimetype, contentLength);
                }
            }

            private void actionShare(String url) {
                WebUtils.shareWeb(BrowserActivity.this, url, null);
            }
        });

        web.setOnCustomWebViewStateChangeListener(mOnWebStateChangeListener);

        WebSettings setting = web.getSettings();
        setting.setNeedInitialFocus(false);
        setting.setDefaultFontSize(16);
        setting.setDefaultFixedFontSize(13);
        setting.setMinimumLogicalFontSize(AppData.minimum_font.get());
        setting.setMinimumFontSize(AppData.minimum_font.get());

        setting.setMixedContentMode(AppData.mixed_content.get());
        setting.setSupportMultipleWindows(AppData.newtab_blank.get() != BrowserManager.LOAD_URL_TAB_CURRENT);
        WebViewUtils.setTextSize(setting, AppData.text_size.get());
        setting.setJavaScriptEnabled(AppData.javascript.get());


        setting.setAllowContentAccess(AppData.allow_content_access.get());
        setting.setAllowFileAccess(AppData.file_access.get() == PreferenceConstants.FILE_ACCESS_ENABLE);
        setting.setDefaultTextEncodingName(AppData.default_encoding.get());
        setting.setUserAgentString(AppData.user_agent.get());
        setting.setLoadWithOverviewMode(AppData.load_overview.get());
        setting.setUseWideViewPort(AppData.web_wideview.get());
        WebViewUtils.setDisplayZoomButtons(setting, AppData.show_zoom_button.get());
        setting.setCacheMode(AppData.web_cache.get());
        setting.setJavaScriptCanOpenWindowsAutomatically(AppData.web_popup.get());
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.valueOf(AppData.layout_algorithm.get()));
        setting.setLoadsImagesAutomatically(!AppData.block_web_images.get());

        boolean noPrivate = !AppData.private_mode.get();
        setting.setDatabaseEnabled(noPrivate && AppData.web_db.get());
        setting.setDomStorageEnabled(noPrivate && AppData.web_dom_db.get());
        setting.setGeolocationEnabled(noPrivate && AppData.web_geolocation.get());
        setting.setAppCacheEnabled(noPrivate && AppData.web_app_cache.get());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setting.setSafeBrowsingEnabled(AppData.safe_browsing.get());
        } else {
            //noinspection deprecation
            setting.setSaveFormData(noPrivate && AppData.save_formdata.get());
        }

        setting.setAppCachePath(BrowserManager.getAppCacheFilePath(getApplicationContext()));

        web.resetTheme();
        web.setSwipeEnable(AppData.pull_to_refresh.get());

        //if add to this, should also add to CacheWebView#settingWebView
    }


    private void finishQuick(int clearTabNo) {
        finishQuick(clearTabNo, AppData.finish_alert_default.get());
    }

    private void finishQuick(int clearTabNo, int finish_clear) {
        if (clearTabNo >= 0) {
            if (mTabManager.size() >= 2)
                removeTab(clearTabNo);
        }

        if ((finish_clear & 0x01) != 0) {
            BrowserManager.clearCache(getApplicationContext());
        }
        if ((finish_clear & 0x02) != 0) {
            CookieManager.getInstance().removeAllCookies(null);
        }
        if ((finish_clear & 0x04) != 0) {
            BrowserManager.clearWebDatabase();
        }
        if ((finish_clear & 0x08) != 0) {
            WebViewDatabase.getInstance(getApplicationContext()).clearHttpAuthUsernamePassword();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && (finish_clear & 0x10) != 0) {
            //noinspection deprecation
            WebViewDatabase.getInstance(getApplicationContext()).clearFormData();
        }
        if ((finish_clear & 0x20) != 0) {
            BrowserHistoryManager manager = BrowserHistoryManager.getInstance(this);
            manager.deleteAll();
        }
        if ((finish_clear & 0x40) != 0) {
            getContentResolver().delete(SuggestProvider.URI_LOCAL, null, null);
        }
        if ((finish_clear & 0x80) != 0) {
            BrowserManager.clearGeolocation();
        }
        if ((finish_clear & 0x100) != 0) {
            FaviconManager.getInstance(getApplicationContext()).clear();
        }

        mHandler.removeCallbacks(mSaveTabsRunnable);
        if (AppData.save_last_tabs.get() && (finish_clear & 0x1000) == 0) {
            mTabManager.saveData();
        } else if (AppData.save_pinned_tabs.get()) {
            mTabManager.clearExceptPinnedTab();
            mTabManager.saveData();
        } else {
            mTabManager.clear();
        }

        BrowserApplication.setNeedLoad(true);
        finish();
    }

    private void finishAlert(final int clearTabNo) {
        new FinishAlertDialog(this)
                .setPositiveButton(android.R.string.yes)
                .setNegativeButton(android.R.string.no)
                .setNeutralButton(R.string.minimize)
                .setClearTabNo(clearTabNo)
                .show(getSupportFragmentManager());
    }

    private void showSearchBox(String query, int target, boolean openNewTab, boolean reverse) {
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(SearchActivity.EXTRA_QUERY, query);
        intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag());
        intent.putExtra(SearchActivity.EXTRA_REVERSE, reverse);

        Bundle appdata = new Bundle();
        appdata.putInt(APPDATA_EXTRA_TARGET, target);
        intent.putExtra(SearchActivity.EXTRA_APP_DATA, appdata);
        intent.putExtra(SearchActivity.EXTRA_OPEN_NEW_TAB, openNewTab);

        startActivityForResult(intent, RESULT_REQUEST_SEARCHBOX);
    }

    private void showTabHistory(int target) {
        final MainTabData tab = mTabManager.get(target);
        final CustomWebBackForwardList history_list = tab.mWebView.copyMyBackForwardList();

        ArrayAdapter<CustomWebHistoryItem> adapter = new ArrayAdapter<CustomWebHistoryItem>(getApplicationContext(), 0, history_list) {
            @Override
            @NonNull
            public View getView(int position, View view, @NonNull ViewGroup parent) {
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.tab_history_list_item, null);
                }
                CustomWebHistoryItem item = getItem(position);
                if (item != null) {
                    ((TextView) view.findViewById(R.id.siteTitleText)).setText(item.getTitle());
                    ((TextView) view.findViewById(R.id.siteUrlText)).setText(item.getUrl());
                    ((ImageView) view.findViewById(R.id.siteIconImageView)).setImageBitmap(item.getFavicon());
                }
                return view;
            }
        };

        ListView listview = new ListView(this);
        listview.setAdapter(adapter);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.tab_history)
                .setView(listview)
                .show();

        listview.setOnItemClickListener((parent, view, position, id) -> {
            int next = position - history_list.getCurrent();
            if (tab.mWebView.canGoBackOrForward(next)) {
                if (tab.isNavLock()) {
                    CustomWebHistoryItem item = history_list.getBackOrForward(next);
                    if (item != null)
                        performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, item.getUrl(), TabType.DEFAULT);
                    else
                        tab.mWebView.goBackOrForward(next);
                } else {
                    tab.mWebView.goBackOrForward(next);
                }
            }
            dialog.dismiss();
        });
    }

    private void setPrivateMode(boolean isPrivate) {
        boolean noPrivate = !isPrivate;
        boolean enable_cookie = isPrivate
                ? AppData.accept_cookie.get() && AppData.accept_cookie_private.get()
                : AppData.accept_cookie.get();

        if (noPrivate && AppData.save_history.get()) {
            if (mBrowserHistoryManager == null)
                mBrowserHistoryManager = new BrowserHistoryAsyncManager(getApplicationContext());
        } else {
            if (mBrowserHistoryManager != null)
                mBrowserHistoryManager.destroy();
            mBrowserHistoryManager = null;
        }
        CookieManager.getInstance().setAcceptCookie(enable_cookie);

        WebSettings setting;

        for (MainTabData tabData : mTabManager.getLoadedData()) {
            setting = tabData.mWebView.getSettings();

            tabData.mWebView.setAcceptThirdPartyCookies(
                    CookieManager.getInstance(), enable_cookie && AppData.accept_third_cookie.get());

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                //noinspection deprecation
                setting.setSaveFormData(noPrivate && AppData.save_formdata.get());
            }
            setting.setDatabaseEnabled(noPrivate && AppData.web_db.get());
            setting.setDomStorageEnabled(noPrivate && AppData.web_dom_db.get());
            setting.setGeolocationEnabled(noPrivate && AppData.web_geolocation.get());
            setting.setAppCacheEnabled(noPrivate && AppData.web_app_cache.get());
            setting.setAppCachePath(BrowserManager.getAppCacheFilePath(getApplicationContext()));
        }

        mToolbar.notifyChangeWebState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String permission;
        int grantResult;
        for (int i = 0; permissions.length > i; i++) {
            permission = permissions[i];
            grantResult = grantResults[i];
            switch (permission) {
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        AppData.web_geolocation.set(false);
                        AppData.commit(BrowserActivity.this, AppData.web_geolocation);
                    }
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        PermissionUtils.setNoNeed(this, false);
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            PermissionUtils.requestStorage(this);
                        } else {
                            if (!(getSupportFragmentManager().findFragmentByTag("permission") instanceof PermissionDialog)) {
                                dialogHandler.sendMessage(dialogHandler.obtainMessage(PermissionDialogHandler.SHOW_DIALOG));
                            }
                        }
                    } else {
                        PermissionUtils.setNoNeed(this, true);
                    }
                    break;
            }
        }
    }

    private static class PermissionDialogHandler extends PauseHandler {

        static final int SHOW_DIALOG = 1;
        private WeakReference<AppCompatActivity> activityReference;

        PermissionDialogHandler(AppCompatActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected boolean storeMessage(Message message) {
            return true;
        }

        @Override
        protected void processMessage(Message message) {
            switch (message.what) {
                case SHOW_DIALOG: {
                    AppCompatActivity activity = activityReference.get();
                    if (activity != null && !PermissionUtils.checkWriteStorage(activity)) {
                        new PermissionDialog().show(activity.getSupportFragmentManager(), "permission");
                    }
                    break;
                }
            }
        }
    }

    public static class PermissionDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.permission_probrem)
                    .setMessage(R.string.confirm_permission_storage_app)
                    .setPositiveButton(android.R.string.ok, (dialog, which) ->
                            PermissionUtils.openRequestPermissionSettings(getActivity(), getString(R.string.request_permission_storage_setting)))
                    .setNegativeButton(android.R.string.no, (dialog, which) -> getActivity().finish());
            setCancelable(false);
            return builder.create();
        }
    }

    private Runnable takeCurrentTabScreen = new Runnable() {
        @Override
        public void run() {
            MainTabData data = mTabManager.getCurrentTabData();
            if (data != null && data.isShotThumbnail())
                mTabManager.forceTakeThumbnail(mTabManager.getCurrentTabData());
        }
    };

    private class Toolbar extends ToolbarManager implements TabLayout.OnTabClickListener {
        private final ActionCallback.TargetInfo mTargetInfoCache = new ActionCallback.TargetInfo();
        private final TabActionManager mTabActionManager;

        public Toolbar() {
            super(BrowserActivity.this, mActionCallback, new ToolbarManager.RequestCallback() {
                @Override
                public boolean shouldShowToolbar(ToolbarVisibilityContainter visibility, MainTabData tabdata) {
                    return shouldShowToolbar(visibility, tabdata, null);
                }

                @Override
                public boolean shouldShowToolbar(ToolbarVisibilityContainter visibility, MainTabData tabdata, Configuration newConfig) {
                    if (!visibility.isVisible())
                        return false;

                    if (mIsFullScreenMode && visibility.isHideWhenFullscreen())
                        return false;

                    Configuration configuration = (newConfig != null) ? newConfig : getResources().getConfiguration();
                    int orientation = configuration.orientation;
                    if (visibility.isHideWhenPortrait() && orientation == Configuration.ORIENTATION_PORTRAIT)
                        return false;

                    if (visibility.isHideWhenLandscape() && orientation == Configuration.ORIENTATION_LANDSCAPE)
                        return false;

                    if (visibility.isHideWhenLayoutShrink() && mIsImeShown)
                        return false;

                    if (tabdata == null) {
                        tabdata = mTabManager.getCurrentTabData();
                        if (tabdata == null)
                            return visibility.isVisible();
                    }

                    return !(visibility.isHideWhenEndLoading() && !tabdata.isInPageLoad());

                }
            });

            mTabActionManager = TabActionManager.getInstance(getApplicationContext());
            setOnTabClickListener(this);
        }

        @Override
        public boolean onTabTouch(View v, MotionEvent ev, int id, boolean selected) {
            return false;
        }

        @Override
        public void onTabSwipeUp(int id) {
            mTargetInfoCache.setTarget(id);
            mActionCallback.run(mTabActionManager.tab_up.action, mTargetInfoCache);
        }

        @Override
        public void onTabSwipeDown(int id) {
            mTargetInfoCache.setTarget(id);
            mActionCallback.run(mTabActionManager.tab_down.action, mTargetInfoCache);
        }

        @Override
        public void onTabLongClick(int id) {
            mTargetInfoCache.setTarget(id);
            mActionCallback.run(mTabActionManager.tab_lpress.action, mTargetInfoCache);
        }

        @Override
        public void onTabDoubleClick(int id) {
            mTargetInfoCache.setTarget(id);
            mActionCallback.run(mTabActionManager.tab_press.action, mTargetInfoCache);
        }

        @Override
        public void onTabChangeClick(int from, int to) {
            setCurrentTab(to);
        }

        @Override
        public void onChangeCurrentTab(int from, int to) {
            MainTabData from_data = mTabManager.get(from);
            MainTabData to_data = mTabManager.get(to);

            Resources res = getResources();

            if (from_data != null)
                from_data.onMoveTabToBackground(res, getTheme());

            if (to_data != null)
                to_data.onMoveTabToForeground(res, getTheme());
        }
    }

    private class MyWebViewClient extends CustomWebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(CustomWebView web, String url, Uri uri) {
            MainTabData data = mTabManager.get(web);
            if (data == null)
                return true;

            if (AppData.file_access.get() == PreferenceConstants.FILE_ACCESS_SAFER && URLUtil.isFileUrl(url)) {
                url = SafeFileProvider.convertToSaferUrl(url);
                loadUrl(data, url);
                return true;
            }
            int patternResult = checkPatternMatch(data, url, false);
            if (patternResult == 0) {
                web.loadUrl(url);
                return true;
            }

            if (patternResult == 1 || checkNewTabLinkAuto(getNewTabPerformType(data), data, url)) {
                if (web.getUrl() == null || data.mWebView.isBackForwardListEmpty()) {
                    removeTab(mTabManager.indexOf(data.getId()));
                }
                return true;
            }


            return preload(data, url, uri);
        }

        @Override
        public void onPageStarted(CustomWebView web, String url, Bitmap favicon) {
            MainTabData data = mTabManager.get(web);
            if (data == null) return;

            applyUserScript(web, url, true);

            data.onPageStarted(url, favicon);

            if (data == mTabManager.getCurrentTabData()) {
                mToolbar.notifyChangeWebState(data);
            }

            if (mIsActivityPaused) {
                resumeWebViewTimers(data);
            }

            if (mWebViewAutoScrollManager != null)
                mWebViewAutoScrollManager.stop();

            data.onStartPage();

            if (AppData.save_tabs_for_crash.get())
                mTabManager.saveData();

            mTabManager.removeThumbnailCache(url);
        }

        @Override
        public void onPageFinished(CustomWebView web, String url) {
            MainTabData data = mTabManager.get(web);
            if (data == null) return;

            applyUserScript(web, url, false);

            if (mIsActivityPaused) {
                pauseWebViewTimers(data);
            }

            data.onPageFinished(web, url);

            if (data == mTabManager.getCurrentTabData()) {
                mToolbar.notifyChangeWebState(data);
            }

            mTabManager.takeThumbnailIfNeeded(data);

            if (AppData.save_tabs_for_crash.get())
                mTabManager.saveData();
        }

        @Override
        public void onFormResubmission(CustomWebView view, final Message dontResend, final Message resend) {
            (new AlertDialog.Builder(BrowserActivity.this))
                    .setTitle(view.getUrl())
                    .setMessage(R.string.form_resubmit)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> resend.sendToTarget())
                    .setNegativeButton(android.R.string.no, (dialog, which) -> dontResend.sendToTarget())
                    .setOnCancelListener(dialog -> dontResend.sendToTarget())
                    .show();
        }

        @Override
        public void doUpdateVisitedHistory(CustomWebView view, String url, boolean isReload) {
            MainTabData data = mTabManager.get(view);
            if (data == null) return;

            if (mBrowserHistoryManager != null)
                mBrowserHistoryManager.add(data.getOriginalUrl());
        }

        @Override
        public void onReceivedHttpAuthRequest(CustomWebView view, HttpAuthHandler handler, String host, String realm) {
            new HttpAuthRequestDialog(BrowserActivity.this).requestHttpAuth(view, handler, host, realm);
        }

        @Override
        public void onReceivedSslError(CustomWebView view, final SslErrorHandler handler, SslError error) {
            if (!AppData.ssl_error_alert.get()) {
                handler.cancel();
                return;
            }

            (new AlertDialog.Builder(BrowserActivity.this))
                    .setTitle(R.string.ssl_error_title)
                    .setMessage(R.string.ssl_error_mes)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> handler.proceed())
                    .setNegativeButton(android.R.string.no, (dialog, which) -> handler.cancel())
                    .setOnCancelListener(dialog -> handler.cancel())
                    .show();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(CustomWebView view, WebResourceRequest request) {
            if ("yuzu".equalsIgnoreCase(request.getUrl().getScheme())) {
                String action = request.getUrl().getSchemeSpecificPart();

                if ("speeddial".equalsIgnoreCase(action)) {
                    return speedDialHtml.createResponse();
                } else if ("speeddial/base.css".equals(action)) {
                    return speedDialHtml.getBaseCss();
                } else if ("speeddial/custom.css".equals(action)) {
                    return speedDialHtml.getCustomCss();
                }
            }

            if (adBlockController != null) {
                TabIndexData tabIndexData = mTabManager.getIndexData(view.getIdentityId());
                Uri uri = null;
                if (tabIndexData != null && tabIndexData.getOriginalUrl() != null)
                    uri = Uri.parse(tabIndexData.getOriginalUrl());

                if (adBlockController.isBlock(uri, request.getUrl())) {
                    return adBlockController.createDummy(request.getUrl());
                }
            }
            if (mResourceCheckerList != null) {
                for (ResourceChecker checker : mResourceCheckerList) {
                    switch (checker.check(request.getUrl())) {
                        case ResourceChecker.SHOULD_RUN:
                            return checker.getResource(getApplicationContext());
                        case ResourceChecker.SHOULD_BREAK:
                            return null;
                        case ResourceChecker.SHOULD_CONTINUE:
                            continue;
                        default:
                            throw new RuntimeException("unknown : " + checker.check(request.getUrl()));
                    }
                }
            }
            return null;
        }
    }

    private class MyWebChromeClient extends CustomWebChromeClient {
        private GeolocationPermissionToolbar geoView = null;

        @Override
        public void onProgressChanged(CustomWebView web, int newProgress) {
            MainTabData data = mTabManager.get(web);
            if (data == null) return;

            data.onProgressChanged(newProgress);
            if (newProgress == 100) {
                CookieManager.getInstance().flush();
            }

            if (data == mTabManager.getCurrentTabData()) {
                if (data.isInPageLoad())
                    mToolbar.notifyChangeProgress(data);
                else
                    mToolbar.notifyChangeWebState(data);
            }

            if (data.isStartDocument() && newProgress > 35) {
                applyUserScript(web, data.getUrl(), true);
                data.setStartDocument(false);
            }
        }

        @Override
        public void onReceivedTitle(CustomWebView web, String title) {
            MainTabData data = mTabManager.get(web);
            if (data == null) return;

            data.onReceivedTitle(title);

            if (mBrowserHistoryManager != null)
                mBrowserHistoryManager.update(data.getOriginalUrl(), title);
        }

        @Override
        public void onReceivedIcon(CustomWebView web, Bitmap icon) {
            MainTabData data = mTabManager.get(web);
            if (data == null) return;

            mSpeedDialAsyncManager.updateAsync(data.getOriginalUrl(), icon);
            mFaviconAsyncManager.updateAsync(data.getOriginalUrl(), icon);

            data.onReceivedIcon(icon);
        }

        @Override
        public void onRequestFocus(CustomWebView web) {
            int i = mTabManager.indexOf(web.getIdentityId());
            if (i >= 0)
                setCurrentTab(i);
        }


        @Override
        public boolean onCreateWindow(CustomWebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            checkNewTabLink(AppData.newtab_blank.get(), (WebViewTransport) resultMsg.obj);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(CustomWebView web) {
            int i = mTabManager.indexOf(web.getIdentityId());
            if (i >= 0)
                removeTab(i);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (mWebUploadHandler == null)
                mWebUploadHandler = new WebUploadHandler();

            try {
                startActivityForResult(Intent.createChooser(mWebUploadHandler.onShowFileChooser(filePathCallback, fileChooserParams), getString(R.string.select_file)), RESULT_REQUEST_WEB_UPLOAD);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.app_notfound, Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onJsAlert(CustomWebView view, String url, String message, final JsResult result) {
            (new AlertDialog.Builder(BrowserActivity.this))
                    .setTitle(url)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> result.confirm())
                    .setOnCancelListener(dialog -> result.cancel())
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(CustomWebView view, String url, String message, final JsResult result) {
            if (!isFinishing())
                (new AlertDialog.Builder(BrowserActivity.this))
                        .setTitle(url)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> result.confirm())
                        .setNegativeButton(android.R.string.no, (dialog, which) -> result.cancel())
                        .setOnCancelListener(dialog -> result.cancel())
                        .show();
            return true;
        }

        @Override
        public boolean onJsPrompt(CustomWebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            final EditText edit_text = new EditText(BrowserActivity.this);
            edit_text.setText(defaultValue);
            (new AlertDialog.Builder(BrowserActivity.this))
                    .setTitle(url)
                    .setMessage(message)
                    .setView(edit_text)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm(edit_text.getText().toString()))
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> result.cancel())
                    .setOnCancelListener(dialog -> result.cancel())
                    .show();
            return true;
        }


        @Override
        public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
            if (mWebCustomViewHandler == null)
                mWebCustomViewHandler = new WebCustomViewHandler(findViewById(R.id.fullscreenLayout));
            mWebCustomViewHandler.showCustomView(BrowserActivity.this, view, AppData.web_customview_oritentation.get(), callback);
        }

        @Override
        public void onHideCustomView() {
            if (mWebCustomViewHandler != null)
                mWebCustomViewHandler.hideCustomView(BrowserActivity.this);
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (mVideoLoadingProgressView == null) {
                mVideoLoadingProgressView = getLayoutInflater().inflate(R.layout.video_loading, null);
            }
            return mVideoLoadingProgressView;
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            if (geoView != null) {
                mToolbar.hideGeolocationPrmissionPrompt(geoView);
                geoView = null;
            }
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            if (geoView == null) {
                geoView = new GeolocationPermissionToolbar(BrowserActivity.this) {
                    public void onHideToolbar() {
                        mToolbar.hideGeolocationPrmissionPrompt(geoView);
                        geoView = null;
                    }
                };
                mToolbar.showGeolocationPrmissionPrompt(geoView);
            }
            geoView.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        @Override
        public void getVisitedHistory(final ValueCallback<String[]> callback) {
            if (mBrowserHistoryManager != null) {
                new Thread(() -> callback.onReceiveValue(mBrowserHistoryManager.getHistoryArray(3000))).start();
            }
        }
    }

    private class MyOnWebStateChangeListener implements OnWebStateChangeListener {
        @Override
        public void onStateChanged(CustomWebView web, TabData tabdata) {
            MainTabData tab = mTabManager.get(web);
            if (tab == null)
                return;

            tab.onStateChanged(tabdata);

            if (tab == mTabManager.getCurrentTabData()) {
                mToolbar.notifyChangeWebState(tab);
            }
        }
    }

    private class MyOnCreateContextMenuListener extends CustomOnCreateContextMenuListener {
        @Override
        public void onCreateContextMenu(ContextMenu menu, final CustomWebView webview, ContextMenuInfo menuInfo) {
            WebView.HitTestResult result = webview.getHitTestResult();
            if (result == null) return;

            LongPressActionManager manager = LongPressActionManager.getInstance(getApplicationContext());

            switch (result.getType()) {
                case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                    mActionCallback.run(manager.link.action, new HitTestResultTargetInfo(webview, result));
                    break;
                case WebView.HitTestResult.IMAGE_TYPE:
                    mActionCallback.run(manager.image.action, new HitTestResultTargetInfo(webview, result));
                    break;
                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                    mActionCallback.run(manager.image_link.action, new HitTestResultTargetInfo(webview, result));
                    break;
                case WebView.HitTestResult.PHONE_TYPE: {
                    final String extra = result.getExtra();
                    menu.setHeaderTitle(Uri.decode(extra));
                    menu.add(R.string.dial).setOnMenuItemClickListener(arg0 -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_TEL + extra));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return false;
                    });
                    menu.add(R.string.add_contact).setOnMenuItemClickListener(arg0 -> {
                        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        intent.putExtra(Insert.PHONE, Uri.decode(extra));
                        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return false;
                    });
                    menu.add(R.string.copy_phone_num).setOnMenuItemClickListener(arg0 -> {
                        ClipboardUtils.setClipboardText(getApplicationContext(), Uri.decode(extra));
                        return false;
                    });
                }
                break;

                case WebView.HitTestResult.EMAIL_TYPE: {
                    final String extra = result.getExtra();
                    menu.setHeaderTitle(extra);
                    menu.add(R.string.email).setOnMenuItemClickListener(arg0 -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_MAILTO + extra));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return false;
                    });
                    menu.add(R.string.add_contact).setOnMenuItemClickListener(arg0 -> {
                        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        intent.putExtra(Insert.EMAIL, extra);
                        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return false;
                    });
                    menu.add(R.string.copy_email_address).setOnMenuItemClickListener(arg0 -> {
                        ClipboardUtils.setClipboardText(getApplicationContext(), extra);
                        return false;
                    });
                }
                break;

                case WebView.HitTestResult.GEO_TYPE: {
                    final String extra = result.getExtra();
                    menu.setHeaderTitle(extra);
                    menu.add(R.string.open_map).setOnMenuItemClickListener(arg0 -> {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_GEO + URLEncoder.encode(extra, "UTF-8"))));
                        } catch (UnsupportedEncodingException e) {
                            ErrorReport.printAndWriteLog(e);
                        }
                        return false;
                    });
                    menu.add(R.string.copy_map_address).setOnMenuItemClickListener(arg0 -> {
                        ClipboardUtils.setClipboardText(getApplicationContext(), extra);
                        return false;
                    });
                }
                break;

                case WebView.HitTestResult.UNKNOWN_TYPE:
                    mActionCallback.run(manager.others.action);
                    break;
            }
        }
    }

    private static class PieControl extends PieControlBase {
        public PieControl(Context context, ActionCallback actionCallback) {
            super(context, actionCallback);
        }

        @Override
        protected void makeItems() {
            QuickControlActionManager mananger = QuickControlActionManager.getInstance(getContext());

            for (Action action : mananger.level1.list)
                addItem(action, 1);
            for (Action action : mananger.level2.list)
                addItem(action, 2);
            for (Action action : mananger.level3.list)
                addItem(action, 3);
        }

        public void onPreferenceReset() {
            float density = DisplayUtils.getDensity(getContext());
            PieMenu pie = getPieMenu();
            pie.setRadiusStart((int) (AppData.qc_rad_start.get() * density + 0.5f));
            pie.setRadiusIncrement((int) (AppData.qc_rad_inc.get() * density + 0.5f));
            pie.setSlop((int) (AppData.qc_slop.get() * density + 0.5f));
            pie.setPosition(AppData.qc_position.get());
        }

        public void onThemeChanged(ThemeData themedata) {
            PieMenu pie = getPieMenu();

            if (themedata != null && themedata.qcItemBackgroundColorNormal != 0)
                pie.setNormalColor(themedata.qcItemBackgroundColorNormal);
            else
                pie.setNormalColor(ResourcesCompat.getColor(getContext().getResources(), R.color.qc_normal, getContext().getTheme()));

            if (themedata != null && themedata.qcItemBackgroundColorSelect != 0)
                pie.setSelectedColor(themedata.qcItemBackgroundColorSelect);
            else
                pie.setSelectedColor(ResourcesCompat.getColor(getContext().getResources(), R.color.qc_selected, getContext().getTheme()));

            if (themedata != null && themedata.qcItemColor != 0)
                pie.setColorFilterToItems(themedata.qcItemColor);
            else
                pie.setColorFilterToItems(0);
        }
    }

    private static class WebImageHandler extends WebSrcImageHandler {
        private final WeakReference<BrowserActivity> refBrowserActivity;
        private String userAgent;

        public WebImageHandler(BrowserActivity activity, String userAgent) {
            refBrowserActivity = new WeakReference<>(activity);
            this.userAgent = userAgent;
        }


        @Override
        public void handleUrl(String url) {
            BrowserActivity activity = refBrowserActivity.get();
            if (activity != null)
                DownloadDialog.showDownloadDialog(activity, url, userAgent);//TODO referer
        }
    }

    private static class HitTestResultTargetInfo extends ActionCallback.TargetInfo {
        private final CustomWebView mWebView;
        private final WebView.HitTestResult mResult;
        private ActionNameArray mActionNameArray;

        public HitTestResultTargetInfo(CustomWebView webview, WebView.HitTestResult result) {
            this.mWebView = webview;
            this.mResult = result;
        }

        public CustomWebView getWebView() {
            return mWebView;
        }

        public WebView.HitTestResult getResult() {
            return mResult;
        }

        public ActionNameArray getActionNameArray() {
            Context context = mWebView.getView().getContext();

            if (mActionNameArray == null) {
                switch (mResult.getType()) {
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                        mActionNameArray = new ActionNameArray(context, R.array.pref_lpress_link_list, R.array.pref_lpress_link_values);
                        break;
                    case WebView.HitTestResult.IMAGE_TYPE:
                        mActionNameArray = new ActionNameArray(context, R.array.pref_lpress_image_list, R.array.pref_lpress_image_values);
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                        mActionNameArray = new ActionNameArray(context, R.array.pref_lpress_linkimage_list, R.array.pref_lpress_linkimage_values);
                        break;
                }
            }
            return mActionNameArray;
        }
    }

    private class MyActionCallback extends ActionCallback {
        public boolean run(Action list, HitTestResultTargetInfo target) {
            if (list.isEmpty()) return false;
            for (SingleAction action : list) {
                run(action, target);
            }
            return true;
        }

        public boolean run(SingleAction action, HitTestResultTargetInfo target) {
            WebView.HitTestResult result = target.getResult();
            final CustomWebView webview = target.getWebView();

            switch (result.getType()) {
                case WebView.HitTestResult.SRC_ANCHOR_TYPE: {
                    final String extra = result.getExtra();//URL
                    switch (action.id) {
                        case SingleAction.LPRESS_OPEN:
                            webview.loadUrl(extra);
                            return true;
                        case SingleAction.LPRESS_OPEN_NEW:
                            openInNewTabPost(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_BG:
                            openInBackground(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_NEW_RIGHT:
                            openInRightNewTabPost(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_BG_RIGHT:
                            openInRightBgTab(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_SHARE:
                            WebUtils.shareWeb(BrowserActivity.this, extra, null);
                            return true;
                        case SingleAction.LPRESS_OPEN_OTHERS:
                            startActivity(PackageUtils.createChooser(BrowserActivity.this, extra, getText(R.string.open_other_app)));
                            return true;
                        case SingleAction.LPRESS_COPY_URL:
                            ClipboardUtils.setClipboardText(getApplicationContext(), extra);
                            return true;
                        case SingleAction.LPRESS_SAVE_PAGE_AS:
                            DownloadDialog.showDownloadDialog(BrowserActivity.this, extra, webview.getSettings().getUserAgentString());//TODO referer
                            return true;
                        case SingleAction.LPRESS_SAVE_PAGE:
                            DownloadRequestInfo info = new DownloadRequestInfo(extra, null, null, webview.getSettings().getUserAgentString(), -1);
                            DownloadService.startDownloadService(BrowserActivity.this, info);
                            return true;
                        case SingleAction.LPRESS_PATTERN_MATCH: {
                            Intent intent = new Intent(BrowserActivity.this, PatternUrlActivity.class);
                            intent.putExtra(Intent.EXTRA_TEXT, extra);
                            startActivity(intent);
                            return true;
                        }
                        case SingleAction.LPRESS_COPY_LINK_TEXT:
                            webview.requestFocusNodeHref(new WebSrcLinkCopyHandler(BrowserActivity.this).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_ADD_BLACK_LIST:
                            AddAdBlockDialog.addBackListInstance(extra)
                                    .show(getSupportFragmentManager(), "add black");
                            return true;
                        case SingleAction.LPRESS_ADD_WHITE_LIST:
                            AddAdBlockDialog.addWhiteListInstance(extra)
                                    .show(getSupportFragmentManager(), "add white");
                            return true;
                        default:
                            return run(action, target, null);
                    }
                }
                case WebView.HitTestResult.IMAGE_TYPE: {
                    final String extra = result.getExtra();//image URL
                    switch (action.id) {
                        case SingleAction.LPRESS_OPEN_IMAGE:
                            webview.loadUrl(extra);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_NEW:
                            openInNewTabPost(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_BG:
                            openInBackground(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_NEW_RIGHT:
                            openInRightNewTabPost(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_BG_RIGHT:
                            openInRightBgTab(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_SHARE_IMAGE_URL:
                            WebUtils.shareWeb(BrowserActivity.this, extra, null);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_OTHERS:
                            startActivity(PackageUtils.createChooser(BrowserActivity.this, extra, getText(R.string.open_other_app)));
                            return true;
                        case SingleAction.LPRESS_COPY_IMAGE_URL:
                            ClipboardUtils.setClipboardText(getApplicationContext(), extra);
                            return true;
                        case SingleAction.LPRESS_SAVE_IMAGE_AS:
                            DownloadDialog.showDownloadDialog(BrowserActivity.this, extra, webview.getUrl(), webview.getSettings().getUserAgentString(), ".jpg");
                            return true;
                        case SingleAction.LPRESS_GOOGLE_IMAGE_SEARCH:
                            openInNewTabPost(SearchUtils.makeGoogleImageSearch(extra), TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_IMAGE_RES_BLOCK: {
                            Intent intent = new Intent(BrowserActivity.this, ResourceBlockListActivity.class);
                            intent.setAction(ResourceBlockListActivity.ACTION_BLOCK_IMAGE);
                            intent.putExtra(Intent.EXTRA_TEXT, extra);
                            startActivity(intent);
                            return true;
                        }
                        case SingleAction.LPRESS_PATTERN_MATCH: {
                            Intent intent = new Intent(BrowserActivity.this, PatternUrlActivity.class);
                            intent.putExtra(Intent.EXTRA_TEXT, extra);
                            startActivity(intent);
                            return true;
                        }
                        case SingleAction.LPRESS_SHARE_IMAGE: {
                            Intent intent = new Intent(BrowserActivity.this, FastDownloadActivity.class);
                            intent.putExtra(FastDownloadActivity.EXTRA_FILE_URL, extra);
                            intent.putExtra(FastDownloadActivity.EXTRA_FILE_REFERER, webview.getUrl());
                            intent.putExtra(FastDownloadActivity.EXTRA_DEFAULT_EXTENSION, ".jpg");
                            startActivityForResult(intent, RESULT_REQUEST_SHARE_IMAGE);
                            return true;
                        }
                        case SingleAction.LPRESS_SAVE_IMAGE:
                            DownloadRequestInfo info = new DownloadRequestInfo(extra, null, webview.getUrl(), webview.getSettings().getUserAgentString(), -1);
                            info.setDefaultExt(".jpg");
                            DownloadService.startDownloadService(BrowserActivity.this, info);
                            return true;
                        case SingleAction.LPRESS_ADD_IMAGE_BLACK_LIST:
                            AddAdBlockDialog.addBackListInstance(extra)
                                    .show(getSupportFragmentManager(), "add black");
                            return true;
                        case SingleAction.LPRESS_ADD_IMAGE_WHITE_LIST:
                            AddAdBlockDialog.addWhiteListInstance(extra)
                                    .show(getSupportFragmentManager(), "add white");
                            return true;
                        default:
                            return run(action, target, null);
                    }
                }
                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: {
                    final String extra = result.getExtra();//image URL
                    switch (action.id) {
                        case SingleAction.LPRESS_OPEN:
                            webview.requestFocusNodeHref(new WebSrcImageLoadUrlHandler(webview).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_OPEN_NEW:
                            webview.requestFocusNodeHref(new WebSrcImageOpenNewTabHandler(BrowserActivity.this).obtainMessage());//TODO check stratActionMode's Nullpo exception
                            return true;
                        case SingleAction.LPRESS_OPEN_BG:
                            webview.requestFocusNodeHref(new WebSrcImageOpenBackgroundHandler(BrowserActivity.this).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_OPEN_NEW_RIGHT:
                            webview.requestFocusNodeHref(new WebSrcImageOpenRightNewTabHandler(BrowserActivity.this).obtainMessage());//TODO check stratActionMode's Nullpo exception
                            return true;
                        case SingleAction.LPRESS_OPEN_BG_RIGHT:
                            webview.requestFocusNodeHref(new WebSrcImageOpenRightBgTabHandler(BrowserActivity.this).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_SHARE:
                            webview.requestFocusNodeHref(new WebSrcImageShareWebHandler(BrowserActivity.this).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_OPEN_OTHERS:
                            webview.requestFocusNodeHref(new WebSrcImageOpenOtherAppHandler(BrowserActivity.this).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_COPY_URL:
                            webview.requestFocusNodeHref(new WebSrcImageCopyUrlHandler(getApplicationContext()).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_SAVE_PAGE_AS:
                            webview.requestFocusNodeHref(new WebImageHandler(BrowserActivity.this, webview.getSettings().getUserAgentString()).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE:
                            webview.loadUrl(extra);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_NEW:
                            openInNewTabPost(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_BG:
                            openInBackground(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_NEW_RIGHT:
                            openInRightNewTabPost(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_BG_RIGHT:
                            openInRightBgTab(extra, TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_SHARE_IMAGE_URL:
                            WebUtils.shareWeb(BrowserActivity.this, extra, null);
                            return true;
                        case SingleAction.LPRESS_OPEN_IMAGE_OTHERS:
                            startActivity(PackageUtils.createChooser(BrowserActivity.this, extra, getText(R.string.open_other_app)));
                            return true;
                        case SingleAction.LPRESS_COPY_IMAGE_URL:
                            ClipboardUtils.setClipboardText(getApplicationContext(), extra);
                            return true;
                        case SingleAction.LPRESS_SAVE_IMAGE_AS:
                            DownloadDialog.showDownloadDialog(BrowserActivity.this, extra, webview.getUrl(), webview.getSettings().getUserAgentString(), ".jpg");
                            return true;
                        case SingleAction.LPRESS_GOOGLE_IMAGE_SEARCH:
                            openInNewTabPost(SearchUtils.makeGoogleImageSearch(extra), TabType.WINDOW);
                            return true;
                        case SingleAction.LPRESS_IMAGE_RES_BLOCK: {
                            Intent intent = new Intent(BrowserActivity.this, ResourceBlockListActivity.class);
                            intent.setAction(ResourceBlockListActivity.ACTION_BLOCK_IMAGE);
                            intent.putExtra(Intent.EXTRA_TEXT, extra);
                            startActivity(intent);
                            return true;
                        }
                        case SingleAction.LPRESS_PATTERN_MATCH: {
                            Intent intent = new Intent(BrowserActivity.this, PatternUrlActivity.class);
                            intent.putExtra(Intent.EXTRA_TEXT, extra);
                            startActivity(intent);
                            return true;
                        }
                        case SingleAction.LPRESS_SHARE_IMAGE: {
                            Intent intent = new Intent(BrowserActivity.this, FastDownloadActivity.class);
                            intent.putExtra(FastDownloadActivity.EXTRA_FILE_URL, extra);
                            intent.putExtra(FastDownloadActivity.EXTRA_FILE_REFERER, webview.getUrl());
                            intent.putExtra(FastDownloadActivity.EXTRA_DEFAULT_EXTENSION, ".jpg");
                            startActivityForResult(intent, RESULT_REQUEST_SHARE_IMAGE);
                            return true;
                        }
                        case SingleAction.LPRESS_SAVE_IMAGE:
                            DownloadRequestInfo info = new DownloadRequestInfo(extra, null, webview.getUrl(), webview.getSettings().getUserAgentString(), -1);
                            info.setDefaultExt(".jpg");
                            DownloadService.startDownloadService(BrowserActivity.this, info);
                            return true;
                        case SingleAction.LPRESS_ADD_BLACK_LIST:
                            webview.requestFocusNodeHref(new WebSrcImageLinkHandler(url -> AddAdBlockDialog.addBackListInstance(url)
                                    .show(getSupportFragmentManager(), "add black")).obtainMessage());
                            return true;
                        case SingleAction.LPRESS_ADD_IMAGE_BLACK_LIST:
                            AddAdBlockDialog.addBackListInstance(extra)
                                    .show(getSupportFragmentManager(), "add black");
                            return true;
                        case SingleAction.LPRESS_ADD_WHITE_LIST:
                            webview.requestFocusNodeHref(new WebSrcImageLinkHandler(url -> AddAdBlockDialog.addWhiteListInstance(url)
                                    .show(getSupportFragmentManager(), "add white")).obtainMessage());
                        case SingleAction.LPRESS_ADD_IMAGE_WHITE_LIST:
                            AddAdBlockDialog.addWhiteListInstance(extra)
                                    .show(getSupportFragmentManager(), "add white");
                            return true;
                        default:
                            return run(action, target, null);
                    }
                }
            }
            return false;
        }

        public boolean checkAndRun(Action action, TargetInfo def_target) {
            if (def_target != null && def_target instanceof HitTestResultTargetInfo)
                return run(action, (HitTestResultTargetInfo) def_target);
            else
                return run(action, def_target);
        }

        @Override
        public boolean run(SingleAction action, final TargetInfo def_target, View button) {
            final int target = (def_target != null && def_target.getTarget() >= 0) ? def_target.getTarget() : mTabManager.getCurrentTabNo();

            if (target < 0 || target >= mTabManager.size()) {
                return false;
            }

            switch (action.id) {
                case SingleAction.GO_BACK: {
                    MainTabData tab = mTabManager.get(target);
                    if (tab.mWebView.canGoBack()) {
                        if (tab.isNavLock()) {
                            CustomWebHistoryItem item = tab.mWebView.copyMyBackForwardList().getPrev();
                            if (item != null) {
                                performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, item.getUrl(), TabType.WINDOW);
                                break;
                            }
                        }
                        tab.mWebView.goBack();
                        superFrameLayout.postDelayed(takeCurrentTabScreen, 500);
                    } else {
                        checkAndRun(((GoBackSingleAction) action).getDefaultAction(), def_target);
                    }
                }
                break;
                case SingleAction.GO_FORWARD: {
                    MainTabData tab = mTabManager.get(target);
                    if (tab.mWebView.canGoForward()) {
                        if (tab.isNavLock()) {
                            CustomWebHistoryItem item = tab.mWebView.copyMyBackForwardList().getNext();
                            if (item != null) {
                                performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, item.getUrl(), TabType.WINDOW);
                                break;
                            }
                        }
                        tab.mWebView.goForward();
                        superFrameLayout.postDelayed(takeCurrentTabScreen, 500);
                    }
                }
                break;
                case SingleAction.WEB_RELOAD_STOP: {
                    MainTabData tab = mTabManager.get(target);
                    if (tab.isInPageLoad())
                        tab.mWebView.stopLoading();
                    else
                        tab.mWebView.reload();
                }
                break;
                case SingleAction.WEB_RELOAD:
                    mTabManager.get(target).mWebView.reload();
                    break;
                case SingleAction.WEB_STOP:
                    mTabManager.get(target).mWebView.stopLoading();
                    break;
                case SingleAction.GO_HOME:
                    loadUrl(mTabManager.get(target), AppData.home_page.get());
                    break;
                case SingleAction.ZOOM_IN:
                    mTabManager.get(target).mWebView.zoomIn();
                    break;
                case SingleAction.ZOOM_OUT:
                    mTabManager.get(target).mWebView.zoomOut();
                    break;
                case SingleAction.PAGE_UP:
                    mTabManager.get(target).mWebView.pageUp(false);
                    break;
                case SingleAction.PAGE_DOWN:
                    mTabManager.get(target).mWebView.pageDown(false);
                    break;
                case SingleAction.PAGE_TOP:
                    mTabManager.get(target).mWebView.pageUp(true);
                    break;
                case SingleAction.PAGE_BOTTOM:
                    mTabManager.get(target).mWebView.pageDown(true);
                    break;
                case SingleAction.PAGE_SCROLL:
                    ((WebScrollSingleAction) action).scrollWebView(getApplicationContext(), mTabManager.get(target).mWebView);
                    break;
                case SingleAction.PAGE_FAST_SCROLL: {
                    if (mWebViewPageFastScroller == null) {
                        mWebViewPageFastScroller = new WebViewPageFastScroller(BrowserActivity.this);
                        mToolbar.getBottomToolbarAlwaysLayout().addView(mWebViewPageFastScroller);
                        mWebViewPageFastScroller.show(mTabManager.get(target).mWebView);
                        mWebViewPageFastScroller.setOnEndListener(() -> {
                            mToolbar.getBottomToolbarAlwaysLayout().removeView(mWebViewPageFastScroller);
                            mWebViewPageFastScroller = null;
                            return true;
                        });
                    } else {
                        mWebViewPageFastScroller.close();
                    }
                }
                break;
                case SingleAction.PAGE_AUTO_SCROLL: {
                    if (mWebViewAutoScrollManager == null) {
                        mWebViewAutoScrollManager = new WebViewAutoScrollManager();
                        mWebViewAutoScrollManager.setOnStopListener(() -> mWebViewAutoScrollManager = null);
                        mWebViewAutoScrollManager.start(mTabManager.get(target).mWebView, ((AutoPageScrollAction) action).getScrollSpeed());
                    } else {
                        mWebViewAutoScrollManager.stop();
                    }
                }
                case SingleAction.FOCUS_UP:
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP));
                    break;
                case SingleAction.FOCUS_DOWN:
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN));
                    break;
                case SingleAction.FOCUS_LEFT:
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
                    break;
                case SingleAction.FOCUS_RIGHT:
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                    break;
                case SingleAction.FOCUS_CLICK:
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER));
                    dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER));
                    break;
                case SingleAction.TOGGLE_JS: {
                    CustomWebView web = mTabManager.get(target).mWebView;
                    boolean to = !web.getSettings().getJavaScriptEnabled();
                    Toast.makeText(getApplicationContext(), (to) ? R.string.toggle_enable : R.string.toggle_disable, Toast.LENGTH_SHORT).show();
                    web.getSettings().setJavaScriptEnabled(to);
                    web.reload();
                }
                break;
                case SingleAction.TOGGLE_IMAGE: {
                    CustomWebView web = mTabManager.get(target).mWebView;
                    boolean to = !web.getSettings().getLoadsImagesAutomatically();
                    Toast.makeText(getApplicationContext(), (to) ? R.string.toggle_enable : R.string.toggle_disable, Toast.LENGTH_SHORT).show();
                    web.getSettings().setLoadsImagesAutomatically(to);
                    web.reload();
                }
                break;
                case SingleAction.TOGGLE_COOKIE: {
                    boolean cookie = !AppData.accept_cookie.get();
                    AppData.accept_cookie.set(cookie);
                    AppData.commit(BrowserActivity.this, AppData.accept_cookie);
                    CookieManager.getInstance().setAcceptCookie(cookie);
                    Toast.makeText(getApplicationContext(), cookie ? R.string.toggle_enable : R.string.toggle_disable, Toast.LENGTH_SHORT).show();
                }
                break;
                case SingleAction.TOGGLE_USERJS: {
                    boolean to = mUserScriptList == null;
                    Toast.makeText(getApplicationContext(), (to) ? R.string.toggle_enable : R.string.toggle_disable, Toast.LENGTH_SHORT).show();
                    resetUserScript(to);
                    MainTabData tab = mTabManager.get(target);
                    if (to) {
                        if (!tab.isInPageLoad())
                            applyUserScript(tab.mWebView, tab.getUrl(), false);
                        mToolbar.notifyChangeWebState();//icon change
                    } else {
                        tab.mWebView.reload();
                    }
                }
                break;
                case SingleAction.TOGGLE_NAV_LOCK: {
                    MainTabData tab = mTabManager.get(target);
                    tab.setNavLock(!tab.isNavLock());
                    tab.invalidateView(target == mTabManager.getCurrentTabNo(), getResources(), getTheme());
                    mToolbar.notifyChangeWebState();//icon change
                }
                break;
                case SingleAction.PAGE_INFO: {
                    MainTabData tab = mTabManager.get(target);

                    View view = getLayoutInflater().inflate(R.layout.page_info_dialog, null);
                    final CopyableTextView titleTextView = view.findViewById(R.id.titleTextView);
                    final CopyableTextView urlTextView = view.findViewById(R.id.urlTextView);

                    titleTextView.setText(tab.getTitle());
                    final String url = tab.getUrl();
                    urlTextView.setText(UrlUtils.decodeUrl(url));
                    urlTextView.setOnLongClickListener(v -> {
                        ClipboardUtils.setClipboardText(BrowserActivity.this, url);
                        return true;
                    });

                    new AlertDialog.Builder(BrowserActivity.this)
                            .setTitle(R.string.page_info)
                            .setView(view)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                break;
                case SingleAction.COPY_URL:
                    ClipboardUtils.setClipboardText(getApplicationContext(), mTabManager.get(target).getUrl());
                    break;
                case SingleAction.COPY_TITLE:
                    ClipboardUtils.setClipboardText(getApplicationContext(), mTabManager.get(target).getTitle());
                    break;
                case SingleAction.COPY_TITLE_URL: {
                    MainTabData tab = mTabManager.get(target);
                    String url = tab.getUrl();
                    String title = tab.getTitle();
                    if (url == null)
                        ClipboardUtils.setClipboardText(getApplicationContext(), title);
                    else if (title == null)
                        ClipboardUtils.setClipboardText(getApplicationContext(), url);
                    else
                        ClipboardUtils.setClipboardText(getApplicationContext(), title + " " + url);
                }
                break;
                case SingleAction.TAB_HISTORY:
                    showTabHistory(target);
                    break;
                case SingleAction.MOUSE_POINTER:
                    if (mCursorView == null) {
                        mCursorView = new PointerView(getApplicationContext());
                        mCursorView.setBackFinish(((MousePointerSingleAction) action).isBackFinish());
                        mCursorView.setView(mTabManager.getCurrentTabData().mWebView.getView());
                        webFrameLayout.addView(mCursorView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    } else {
                        mCursorView.setView(null);
                        webFrameLayout.removeView(mCursorView);
                        mCursorView = null;
                    }
                    break;
                case SingleAction.FIND_ON_PAGE:
                    if (mWebViewFindDialog == null)
                        mWebViewFindDialog = WebViewFindDialogFactory.createInstance(BrowserActivity.this, mToolbar.getFindOnPage());
                    if (mWebViewFindDialog.isVisible())
                        mWebViewFindDialog.hide();
                    else {
                        mWebViewFindDialog.show(mTabManager.get(target).mWebView);
                    }
                    break;
                case SingleAction.SAVE_SCREENSHOT: {
                    SaveScreenshotSingleAction saveSsAction = (SaveScreenshotSingleAction) action;
                    File file = new File(saveSsAction.getFolder(), "ss_" + System.currentTimeMillis() + ".png");
                    int type = saveSsAction.getType();
                    try {
                        switch (type) {
                            case SaveScreenshotSingleAction.SS_TYPE_ALL:
                                if (WebViewUtils.savePictureOverall(mTabManager.get(target).mWebView, file))
                                    Toast.makeText(getApplicationContext(), getString(R.string.saved_file) + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), R.string.failed, Toast.LENGTH_SHORT).show();
                                FileUtils.notifyImageFile(getApplicationContext(), file.getAbsolutePath());
                                break;
                            case SaveScreenshotSingleAction.SS_TYPE_PART:
                                if (WebViewUtils.savePicturePart(mTabManager.get(target).mWebView.getWebView(), file))
                                    Toast.makeText(getApplicationContext(), getString(R.string.saved_file) + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), R.string.failed, Toast.LENGTH_SHORT).show();
                                FileUtils.notifyImageFile(getApplicationContext(), file.getAbsolutePath());
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "Unknown screenshot type : " + type, Toast.LENGTH_LONG).show();
                                break;
                        }
                    } catch (IOException e) {
                        ErrorReport.printAndWriteLog(e);
                        Toast.makeText(getApplicationContext(), "IOException : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
                case SingleAction.SHARE_SCREENSHOT: {
                    File file = new File(getExternalCacheDir(), "ss_" + System.currentTimeMillis() + ".png");
                    int type = ((ShareScreenshotSingleAction) action).getType();
                    try {
                        boolean result = false;
                        switch (type) {
                            case ShareScreenshotSingleAction.SS_TYPE_ALL:
                                result = WebViewUtils.savePictureOverall(mTabManager.get(target).mWebView, file);
                                break;
                            case ShareScreenshotSingleAction.SS_TYPE_PART:
                                result = WebViewUtils.savePicturePart(mTabManager.get(target).mWebView.getWebView(), file);
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "Unknown screenshot type : " + type, Toast.LENGTH_LONG).show();
                                break;
                        }

                        if (result) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("image/png");
                            intent.putExtra(Intent.EXTRA_STREAM, DownloadFileProvider.getUriForFIle(file));
                            try {
                                startActivity(PackageUtils.createChooser(BrowserActivity.this, intent, null));
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.failed, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        ErrorReport.printAndWriteLog(e);
                        Toast.makeText(getApplicationContext(), "IOException : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
                case SingleAction.SAVE_PAGE: {
                    MainTabData tab = mTabManager.get(target);
                    DownloadDialog.showArchiveDownloadDialog(BrowserActivity.this, tab.getUrl(), tab.mWebView);
                }
                break;
                case SingleAction.OPEN_URL: {
                    OpenUrlSingleAction openUrlAction = (OpenUrlSingleAction) action;
                    loadUrl(mTabManager.get(target), openUrlAction.getUrl(), openUrlAction.getTargetTab(), TabType.WINDOW);
                }
                break;
                case SingleAction.TRANSLATE_PAGE: {
                    TranslatePageSingleAction translateAction = (TranslatePageSingleAction) action;
                    final MainTabData tab = mTabManager.get(target);
                    final String from = translateAction.getTranslateFrom();
                    String to = translateAction.getTranslateTo();
                    if (TextUtils.isEmpty(to)) {
                        new AlertDialog.Builder(BrowserActivity.this)
                                .setItems(R.array.translate_language_list, (dialog, which) -> {
                                    String to1 = getResources().getStringArray(R.array.translate_language_values)[which];
                                    String url = URLUtil.composeSearchUrl(tab.getUrl(), "http://translate.google.com/translate?sl=" + from + "&tl=" + to1 + "&u=%s", "%s");
                                    loadUrl(tab, url);
                                })
                                .show();
                    } else {
                        String url = URLUtil.composeSearchUrl(tab.getUrl(), "http://translate.google.com/translate?sl=" + from + "&tl=" + to + "&u=%s", "%s");
                        loadUrl(tab, url);
                    }
                }
                break;
                case SingleAction.NEW_TAB:
                    openInNewTab(AppData.home_page.get(), TabType.DEFAULT);
                    break;
                case SingleAction.CLOSE_TAB:
                    if (!removeTab(target)) {
                        checkAndRun(((CloseTabSingleAction) action).getDefaultAction(), def_target);
                    }
                    break;
                case SingleAction.CLOSE_ALL:
                    openInNewTab(AppData.home_page.get(), TabType.DEFAULT);
                    for (int i = mTabManager.getLastTabNo() - 1; i >= 0; --i) {
                        removeTab(i, false);
                    }
                    break;
                case SingleAction.CLOSE_OTHERS:
                    for (int i = mTabManager.getLastTabNo(); i > target; --i) {
                        removeTab(i, false);
                    }
                    for (int i = target - 1; i >= 0; --i) {
                        removeTab(i, false);
                    }
                    break;
                case SingleAction.CLOSE_AUTO_SELECT:
                    int type = mTabManager.get(target).getTabType();

                    switch (type) {
                        case TabType.DEFAULT:
                            checkAndRun(((CloseAutoSelectAction) action).getDefaultAction(), def_target);
                            break;
                        case TabType.INTENT:
                            checkAndRun(((CloseAutoSelectAction) action).getIntentAction(), def_target);
                            break;
                        case TabType.WINDOW:
                            checkAndRun(((CloseAutoSelectAction) action).getWindowAction(), def_target);
                            break;
                    }

                    break;
                case SingleAction.LEFT_TAB:
                    if (mTabManager.isFirst()) {
                        if (((LeftRightTabSingleAction) action).isTabLoop()) {
                            setCurrentTab(mTabManager.getLastTabNo());
                            mToolbar.scrollTabRight();
                        }
                    } else {
                        int to = mTabManager.getCurrentTabNo() - 1;
                        setCurrentTab(to);
                        mToolbar.scrollTabTo(to);
                    }
                    break;
                case SingleAction.RIGHT_TAB:
                    if (mTabManager.isLast()) {
                        if (((LeftRightTabSingleAction) action).isTabLoop()) {
                            setCurrentTab(0);
                            mToolbar.scrollTabLeft();
                        }
                    } else {
                        int to = mTabManager.getCurrentTabNo() + 1;
                        setCurrentTab(to);
                        mToolbar.scrollTabTo(to);
                    }
                    break;
                case SingleAction.SWAP_LEFT_TAB:
                    if (!mTabManager.isFirst(target)) {
                        int to = target - 1;
                        swapTab(to, target);
                        mToolbar.scrollTabTo(to);
                    }
                    break;
                case SingleAction.SWAP_RIGHT_TAB:
                    if (!mTabManager.isLast(target)) {
                        int to = target + 1;
                        swapTab(to, target);
                        mToolbar.scrollTabTo(to);
                    }
                    break;
                case SingleAction.TAB_LIST: {
                    if (mTabManagerView != null)
                        break;

                    mTabManagerView = new TabListLayout(BrowserActivity.this, ((TabListSingleAction) action).getMode(), ((TabListSingleAction) action).isLeftButton());
                    mTabManagerView.setTabManager(mTabManager);
                    mTabManagerView.setCallback(new TabListLayout.Callback() {
                        @Override
                        public void requestTabListClose() {
                            superFrameLayout.removeView(mTabManagerView);
                            mTabManagerView = null;
                        }

                        @Override
                        public void requestShowTabHistory(int no) {
                            showTabHistory(no);
                        }

                        @Override
                        public void requestAddTab(int index, MainTabData data) {
                            addTab(index, data);
                        }

                        @Override
                        public void requestSelectTab(int no) {
                            setCurrentTab(no);
                            mToolbar.scrollTabTo(no);
                        }

                        @Override
                        public void requestAddTab() {
                            openInNewTab(AppData.home_page.get(), TabType.DEFAULT);
                        }

                        @Override
                        public void requestMoveTab(int positionFrom, int positionTo) {
                            moveTab(positionFrom, positionTo);
                        }

                        @Override
                        public void requestRemoveTab(int no, boolean destroy) {
                            removeTab(no, false, destroy);
                        }
                    });
                    superFrameLayout.addView(mTabManagerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
                break;
                case SingleAction.CLOSE_ALL_LEFT:
                    for (int i = target - 1; i >= 0; --i) {
                        removeTab(i, false);
                    }
                    break;
                case SingleAction.CLOSE_ALL_RIGHT:
                    for (int i = mTabManager.getLastTabNo(); i > target; --i) {
                        removeTab(i, false);
                    }
                    break;
                case SingleAction.RESTORE_TAB: {
                    Bundle bundle;

                    if (mClosedTabs == null || (bundle = mClosedTabs.poll()) == null) {
                        if (AppData.save_closed_tab.get())
                            Toast.makeText(getApplicationContext(), R.string.tab_restored_failed, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), R.string.tab_restored_setting_error, Toast.LENGTH_LONG).show();
                    } else {
                        openInNewTab(bundle);
                        Toast.makeText(getApplicationContext(), R.string.tab_restored_succeed, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                case SingleAction.REPLICATE_TAB: {
                    openInNewTab(mTabManager.get(target));
                }
                break;
                case SingleAction.SHOW_SEARCHBOX:
                    showSearchBox(mTabManager.get(target).getUrl(),
                            target,
                            ((ShowSearchBoxAction) action).isOpenNewTab(),
                            ((ShowSearchBoxAction) action).isReverse());
                    break;
                case SingleAction.PASTE_SEARCHBOX:
                    showSearchBox(ClipboardUtils.getClipboardText(getApplicationContext()),
                            target,
                            ((PasteSearchBoxAction) action).isOpenNewTab(),
                            ((PasteSearchBoxAction) action).isReverse());
                    break;
                case SingleAction.PASTE_GO: {
                    String text = ClipboardUtils.getClipboardText(getApplicationContext());
                    if (TextUtils.isEmpty(text)) {
                        Toast.makeText(getApplicationContext(), R.string.clipboard_empty, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    loadUrl(mTabManager.get(target), WebUtils.makeUrlFromQuery(text, AppData.search_url.get(), "%s"), ((PasteGoSingleAction) action).getTargetTab(), TabType.WINDOW);
                }
                break;
                case SingleAction.SHOW_BOOKMARK: {
                    Intent intent = new Intent(getApplicationContext(), BookmarkActivity.class);
                    intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag());
                    intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, getRequestedOrientation());
                    startActivityForResult(intent, RESULT_REQUEST_BOOKMARK);
                }
                break;
                case SingleAction.SHOW_HISTORY: {
                    Intent intent = new Intent(getApplicationContext(), BrowserHistoryActivity.class);
                    intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag());
                    intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, getRequestedOrientation());
                    startActivityForResult(intent, RESULT_REQUEST_HISTORY);
                }
                break;
                case SingleAction.SHOW_DOWNLOADS: {
                    Intent intent = new Intent(getApplicationContext(), DownloadListActivity.class);
                    intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag());
                    intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, getRequestedOrientation());
                    startActivity(intent);
                }
                break;
                case SingleAction.SHOW_SETTINGS: {
                    Intent intent = new Intent(getApplicationContext(), MainSettingsActivity.class);
                    startActivityForResult(intent, RESULT_REQUEST_SETTING);
                }
                break;
                case SingleAction.OPEN_SPEED_DIAL:
                    loadUrl(mTabManager.get(target), "yuzu:speeddial");
                    break;
                case SingleAction.ADD_BOOKMARK: {
                    MainTabData tab = mTabManager.get(target);
                    AddBookmarkKt.showDialog(BrowserActivity.this, getSupportFragmentManager(), tab.getTitle(), tab.getUrl());
                }
                break;
                case SingleAction.ADD_SPEED_DIAL: {
                    MainTabData tab = mTabManager.get(target);
                    Intent intent = new Intent(BrowserActivity.this, SpeedDialSettingActivity.class);
                    intent.setAction(SpeedDialSettingActivity.ACTION_ADD_SPEED_DIAL);
                    intent.putExtra(Intent.EXTRA_TITLE, tab.getTitle());
                    intent.putExtra(Intent.EXTRA_TEXT, tab.getUrl());
                    intent.putExtra(SpeedDialSettingActivity.EXTRA_ICON, ImageUtils.trimSquare(tab.mWebView.getFavicon(), 200));
                    startActivity(intent);
                }
                break;
                case SingleAction.ADD_PATTERN: {
                    MainTabData tab = mTabManager.get(target);
                    Intent intent = new Intent(BrowserActivity.this, PatternUrlActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, tab.getUrl());
                    startActivity(intent);
                }
                break;
                case SingleAction.ADD_TO_HOME: {
                    MainTabData tab = mTabManager.get(target);
                    Bitmap bitmap = FaviconManager.getInstance(getApplicationContext()).get(tab.getOriginalUrl());
                    PackageUtils.createShortcut(BrowserActivity.this, tab.getTitle(), tab.getUrl(), bitmap);
                    break;
                }
                case SingleAction.SUB_GESTURE: {
                    final GestureManager manager = GestureManager.getInstance(getApplicationContext(), GestureManager.GESTURE_TYPE_SUB);

                    mSubGestureView = new GestureOverlayView(BrowserActivity.this);
                    mSubGestureView.setEventsInterceptionEnabled(true);
                    mSubGestureView.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
                    mSubGestureView.setGestureStrokeWidth(8.0f);
                    mSubGestureView.setBackgroundColor(0x70000000);
                    mSubGestureView.addOnGesturePerformedListener((overlay, gesture) -> {
                        Action action1 = manager.recognize(gesture);
                        if (action1 != null) {
                            mActionCallback.run(action1);
                            superFrameLayout.removeView(mSubGestureView);
                            mSubGestureView = null;
                        }
                    });
                    superFrameLayout.addView(mSubGestureView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
                break;
                case SingleAction.CLEAR_DATA:
                    new ClearBrowserDataAlertDialog(BrowserActivity.this).show(getSupportFragmentManager());
                    break;
                case SingleAction.SHOW_PROXY_SETTING:
                    new ProxySettingDialog(BrowserActivity.this).show(getSupportFragmentManager());
                    break;
                case SingleAction.ORIENTATION_SETTING:
                    new AlertDialog.Builder(BrowserActivity.this)
                            .setItems(R.array.pref_oritentation_list,
                                    (arg0, which) -> setRequestedOrientation(getResources().getIntArray(R.array.pref_oritentation_values)[which]))
                            .show();
                    break;
                case SingleAction.OPEN_LINK_SETTING:
                    new AlertDialog.Builder(BrowserActivity.this)
                            .setItems(R.array.pref_newtab_list,
                                    (arg0, which) -> AppData.newtab_link.set(getResources().getIntArray(R.array.pref_newtab_values)[which]))
                            .show();
                    break;
                case SingleAction.USERAGENT_SETTING: {
                    Intent uaIntent = new Intent(getApplicationContext(), UserAgentListActivity.class);
                    uaIntent.putExtra(Intent.EXTRA_TEXT, mTabManager.get(target).mWebView.getSettings().getUserAgentString());
                    startActivityForResult(uaIntent, RESULT_REQUEST_USERAGENT);
                }
                break;
                case SingleAction.TEXTSIZE_SETTING: {
                    final WebSettings setting = mTabManager.get(target).mWebView.getSettings();
                    new SeekBarDialog(BrowserActivity.this)
                            .setTitle(R.string.pref_text_size)
                            .setSeekMin(1)
                            .setSeekMax(300)
                            .setValue(WebViewUtils.getTextSize(setting))
                            .setPositiveButton(android.R.string.ok, (dialog, which, value) -> WebViewUtils.setTextSize(setting, value))
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
                break;
                case SingleAction.USERJS_SETTING:
                    startActivityForResult(new Intent(getApplicationContext(), UserScriptListActivity.class), RESULT_REQUEST_USERJS_SETTING);
                    break;
                case SingleAction.WEB_ENCODE_SETTING:
                    Intent webEncode = new Intent(getApplicationContext(), WebTextEncodeListActivity.class);
                    webEncode.putExtra(Intent.EXTRA_TEXT, mTabManager.get(target).mWebView.getSettings().getDefaultTextEncodingName());
                    startActivityForResult(webEncode, RESULT_REQUEST_WEB_ENCODE_SETTING);
                    break;
                case SingleAction.DEFALUT_USERAGENT_SETTING: {
                    Intent uaIntent = new Intent(getApplicationContext(), UserAgentListActivity.class);
                    uaIntent.putExtra(Intent.EXTRA_TEXT, mTabManager.get(target).mWebView.getSettings().getUserAgentString());
                    startActivityForResult(uaIntent, RESULT_DEFAULT_REQUEST_USERAGENT);
                }
                break;
                case SingleAction.RENDER_SETTING: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
                    builder.setTitle(R.string.pref_rendering)
                            .setSingleChoiceItems(R.array.pref_rendering_list, webViewRenderingManager.getMode(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    webViewRenderingManager.setMode(which);

                                    for (MainTabData data : mTabManager.getLoadedData()) {
                                        webViewRenderingManager.setWebViewRendering(data.mWebView);
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null);
                    builder.create().show();
                    break;
                }
                case SingleAction.TOGGLE_VISIBLE_TAB:
                    mToolbar.getTabBar().toggleVisibility();
                    break;
                case SingleAction.TOGGLE_VISIBLE_URL:
                    mToolbar.getUrlBar().toggleVisibility();
                    break;
                case SingleAction.TOGGLE_VISIBLE_PROGRESS:
                    mToolbar.getProgressToolBar().toggleVisibility();
                    break;
                case SingleAction.TOGGLE_VISIBLE_CUSTOM:
                    mToolbar.getCustomBar().toggleVisibility();
                    break;
                case SingleAction.TOGGLE_WEB_TITLEBAR:
                    mToolbar.setWebViewTitlebar(mTabManager.getCurrentTabData().mWebView, false);
                    break;
                case SingleAction.TOGGLE_WEB_GESTURE:
                    webGestureOverlayView.setEnabled(!webGestureOverlayView.isEnabled());
                    mToolbar.notifyChangeWebState();//icon change
                    break;
                case SingleAction.TOGGLE_FLICK: {
                    boolean to = !AppData.flick_enable.get();
                    Toast.makeText(getApplicationContext(), (to) ? R.string.toggle_enable : R.string.toggle_disable, Toast.LENGTH_SHORT).show();
                    AppData.flick_enable.set(to);
                    mToolbar.notifyChangeWebState();//icon change
                }
                break;
                case SingleAction.TOGGLE_QUICK_CONTROL: {
                    boolean to = !getQuickControlEnabled();
                    Toast.makeText(getApplicationContext(), (to) ? R.string.toggle_enable : R.string.toggle_disable, Toast.LENGTH_SHORT).show();
                    setQuickControlEnabled(to);
                    mToolbar.notifyChangeWebState();//icon change
                }
                break;
                case SingleAction.TOGGLE_MULTI_FINGER_GESTURE: {
                    boolean to = !getMultiFingerGestureEnabled();
                    Toast.makeText(getApplicationContext(), (to) ? R.string.toggle_enable : R.string.toggle_disable, Toast.LENGTH_SHORT).show();
                    setMultiFingerGestureEnabled(to);
                }
                break;
                case SingleAction.TOGGLE_AD_BLOCK:
                    if (adBlockController == null)
                        adBlockController = new AdBlockController(BrowserActivity.this);
                    else
                        adBlockController = null;
                    mTabManager.get(target).mWebView.reload();
                    if (((WithToastAction) action).getShowToast()) {
                        Toast.makeText(getApplicationContext(),
                                adBlockController != null ? R.string.toggle_enable : R.string.toggle_disable,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SingleAction.OPEN_BLACK_LIST:
                    startActivity(
                            new Intent(BrowserActivity.this, AdBlockActivity.class)
                                    .setAction(AdBlockActivity.ACTION_OPEN_BLACK));
                    break;
                case SingleAction.OPEN_WHITE_LIST:
                    startActivity(
                            new Intent(BrowserActivity.this, AdBlockActivity.class)
                                    .setAction(AdBlockActivity.ACTION_OPEN_WHITE));
                    break;
                case SingleAction.OPEN_WHITE_PATE_LIST:
                    startActivity(
                            new Intent(BrowserActivity.this, AdBlockActivity.class)
                                    .setAction(AdBlockActivity.ACTION_OPEN_WHITE_PAGE));
                    break;
                case SingleAction.ADD_WHITE_LIST_PAGE:
                    AddAdBlockDialog.addWhitePageListInstance(mTabManager.get(target).getUrl())
                            .show(getSupportFragmentManager(), "add white page");
                    break;
                case SingleAction.SHARE_WEB: {
                    MainTabData tab = mTabManager.get(target);
                    WebUtils.shareWeb(BrowserActivity.this, tab.getUrl(), tab.getTitle());
                }
                break;
                case SingleAction.OPEN_OTHER:
                    WebUtils.openInOtherApp(BrowserActivity.this, mTabManager.get(target).getUrl());
                    break;
                case SingleAction.START_ACTIVITY: {
                    Intent intent = ((StartActivitySingleAction) action).getIntent(mTabManager.get(target));
                    if (intent != null) {
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(BrowserActivity.this, R.string.app_notfound, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
                case SingleAction.TOGGLE_FULL_SCREEN:
                    setFullScreenMode(!mIsFullScreenMode);
                    break;
                case SingleAction.OPEN_OPTIONS_MENU:
                    if (button != null)
                        menuWindow.showAsDropDown(button);
                    else
                        menuWindow.show(findViewById(R.id.superFrameLayout), ((OpenOptionsMenuAction) action).getGravity());
                    break;
                case SingleAction.CUSTOM_MENU: {
                    MainTabData tab = mTabManager.get(target);
                    final ActionList actionList = ((CustomMenuSingleAction) action).getActionList();

                    AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);
                    if (def_target instanceof HitTestResultTargetInfo) {
                        HitTestResultTargetInfo web_target = (HitTestResultTargetInfo) def_target;
                        builder.setTitle(web_target.getResult().getExtra())
                                .setAdapter(
                                        new ActionListViewAdapter(BrowserActivity.this, actionList, web_target.getActionNameArray()),
                                        (dialog, which) -> checkAndRun(actionList.get(which), def_target));
                    } else {
                        builder.setTitle(tab.getUrl())
                                .setAdapter(
                                        new ActionListViewAdapter(BrowserActivity.this, actionList, null),
                                        (dialog, which) -> checkAndRun(actionList.get(which), def_target));
                    }

                    builder.show();
                }
                break;
                case SingleAction.FINISH: {
                    FinishSingleAction finishAction = (FinishSingleAction) action;
                    int closeTabTarget = (finishAction.isCloseTab()) ? target : -1;
                    if (finishAction.isShowAlert())
                        finishAlert(closeTabTarget);
                    else
                        finishQuick(closeTabTarget);
                    break;
                }
                case SingleAction.MINIMIZE:
                    moveTaskToBack(true);
                    break;
                case SingleAction.CUSTOM_ACTION:
                    if (def_target instanceof HitTestResultTargetInfo) {
                        return run(((CustomSingleAction) action).getAction(), (HitTestResultTargetInfo) def_target);
                    } else {
                        return run(((CustomSingleAction) action).getAction());
                    }
                case SingleAction.VIBRATION:
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(
                                ((VibrationSingleAction) action).getTime(), VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //noinspection deprecation
                        vibrator.vibrate(((VibrationSingleAction) action).getTime());
                    }
                    break;
                case SingleAction.TOAST:
                    Toast.makeText(getApplicationContext(), ((ToastAction) action).getText(), Toast.LENGTH_SHORT).show();
                    break;
                case SingleAction.PRIVATE:
                    boolean privateMode = !AppData.private_mode.get();
                    AppData.private_mode.set(privateMode);
                    AppData.commit(BrowserActivity.this, AppData.private_mode);
                    setPrivateMode(privateMode);
                    if (((WithToastAction) action).getShowToast()) {
                        Toast.makeText(getApplicationContext(),
                                privateMode ? R.string.toggle_enable : R.string.toggle_disable,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SingleAction.VIEW_SOURCE: {
                    CustomWebView webView = mTabManager.get(target).mWebView;
                    webView.loadUrl("view-source:" + webView.getUrl());
                    break;
                }
                case SingleAction.PRINT:
                    if (PrintHelper.systemSupportsPrint()) {
                        MainTabData tab = mTabManager.get(target);
                        PrintManager manager = (PrintManager) getSystemService(PRINT_SERVICE);
                        String title = tab.getTitle();
                        if (TextUtils.isEmpty(title))
                            title = tab.mWebView.getTitle();
                        if (TextUtils.isEmpty(title))
                            title = "document";
                        String jobName = tab.getUrl();
                        if (TextUtils.isEmpty(jobName))
                            jobName = "about:blank";
                        manager.print(jobName, tab.mWebView.createPrintDocumentAdapter(title), null);
                    } else {
                        Toast.makeText(BrowserActivity.this, R.string.print_not_support, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SingleAction.TAB_PINNING: {
                    MainTabData tab = mTabManager.get(target);
                    tab.setPinning(!tab.isPinning());
                    tab.invalidateView(target == mTabManager.getCurrentTabNo(), getResources(), getTheme());
                    mToolbar.notifyChangeWebState();//icon change
                    break;
                }
                case SingleAction.ALL_ACTION:
                    startActivityForResult(
                            new ActionActivity.Builder(BrowserActivity.this)
                                    .setTitle(R.string.action_list)
                                    .create()
                                    .setAction(ActionActivity.ACTION_ALL_ACTION)
                                    .putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag())
                                    .putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, getRequestedOrientation()),
                            RESULT_REQUEST_ACTION_LIST);
                    break;
                case SingleAction.READER_MODE: {
                    MainTabData tab = mTabManager.get(target);
                    Intent intent = new Intent(BrowserActivity.this, ReaderActivity.class);
                    intent.putExtra(Constants.intent.EXTRA_URL, tab.getOriginalUrl());
                    intent.putExtra(Constants.intent.EXTRA_USER_AGENT, tab.mWebView.getSettings().getUserAgentString());
                    intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, mIsFullScreenMode && DisplayUtils.isNeedFullScreenFlag());
                    intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, getRequestedOrientation());
                    startActivity(intent);
                    break;
                }
                case SingleAction.READ_IT_LATER: {
                    MainTabData tab = mTabManager.get(target);
                    ReadItLaterKt.save(BrowserActivity.this, getContentResolver(), tab.getOriginalUrl(), tab.mWebView);
                    break;
                }
                case SingleAction.READ_IT_LATER_LIST:
                    startActivity(new Intent(BrowserActivity.this, ReadItLaterActivity.class));
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Unknown action:" + action.id, Toast.LENGTH_LONG).show();
                    return false;
            }
            return true;
        }

        @Override
        public Drawable getIcon(SingleAction action) {
            Resources res = getResources();

            switch (action.id) {
                case SingleAction.GO_BACK: {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab == null) return null;
                    if (tab.mWebView.canGoBack())
                        return res.getDrawable(R.drawable.ic_arrow_back_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_arrow_back_disable_white_24dp, getTheme());
                }
                case SingleAction.GO_FORWARD: {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab == null) return null;
                    if (tab.mWebView.canGoForward())
                        return res.getDrawable(R.drawable.ic_arrow_forward_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_arrow_forward_disable_white_24dp, getTheme());
                }
                case SingleAction.WEB_RELOAD_STOP: {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab == null) return null;
                    if (tab.isInPageLoad())
                        return res.getDrawable(R.drawable.ic_clear_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_refresh_white_24px, getTheme());
                }
                case SingleAction.WEB_RELOAD:
                    return res.getDrawable(R.drawable.ic_refresh_white_24px, getTheme());
                case SingleAction.WEB_STOP:
                    return res.getDrawable(R.drawable.ic_clear_white_24dp, getTheme());
                case SingleAction.GO_HOME:
                    return res.getDrawable(R.drawable.ic_home_white_24dp, getTheme());
                case SingleAction.ZOOM_IN:
                    return res.getDrawable(R.drawable.ic_zoom_in_white_24dp, getTheme());
                case SingleAction.ZOOM_OUT:
                    return res.getDrawable(R.drawable.ic_zoom_out_white_24dp, getTheme());
                case SingleAction.PAGE_UP:
                    return res.getDrawable(R.drawable.ic_arrow_upward_white_24dp, getTheme());
                case SingleAction.PAGE_DOWN:
                    return res.getDrawable(R.drawable.ic_arrow_downward_white_24dp, getTheme());
                case SingleAction.PAGE_TOP:
                    return res.getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp, getTheme());
                case SingleAction.PAGE_BOTTOM:
                    return res.getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp, getTheme());
                case SingleAction.PAGE_SCROLL: {
                    int id = ((WebScrollSingleAction) action).getIconResourceId();
                    if (id > 0)
                        return res.getDrawable(((WebScrollSingleAction) action).getIconResourceId(), getTheme());
                    else
                        return null;
                }
                case SingleAction.PAGE_FAST_SCROLL:
                    return res.getDrawable(R.drawable.ic_scroll_white_24dp, getTheme());
                case SingleAction.PAGE_AUTO_SCROLL:
                    return res.getDrawable(R.drawable.ic_play_arrow_white_24dp, getTheme());
                case SingleAction.FOCUS_UP:
                    return res.getDrawable(R.drawable.ic_label_up_white_24px, getTheme());
                case SingleAction.FOCUS_DOWN:
                    return res.getDrawable(R.drawable.ic_label_down_white_24px, getTheme());
                case SingleAction.FOCUS_LEFT:
                    return res.getDrawable(R.drawable.ic_label_left_white_24px, getTheme());
                case SingleAction.FOCUS_RIGHT:
                    return res.getDrawable(R.drawable.ic_label_right_white_24px, getTheme());
                case SingleAction.FOCUS_CLICK:
                    return res.getDrawable(R.drawable.ic_fiber_manual_record_white_24dp, getTheme());
                case SingleAction.TOGGLE_JS: {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab == null) return null;
                    if (tab.mWebView.getSettings().getJavaScriptEnabled())
                        return res.getDrawable(R.drawable.ic_memory_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_memory_white_disable_24px, getTheme());
                }
                case SingleAction.TOGGLE_IMAGE: {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab == null) return null;
                    if (tab.mWebView.getSettings().getLoadsImagesAutomatically())
                        return res.getDrawable(R.drawable.ic_crop_original_white_24px, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_crop_original_disable_white_24px, getTheme());
                }
                case SingleAction.TOGGLE_COOKIE: {
                    if (AppData.accept_cookie.get())
                        return res.getDrawable(R.drawable.ic_cookie_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_cookie_disable_24dp, getTheme());
                }
                case SingleAction.TOGGLE_USERJS: {
                    if (mUserScriptList != null)
                        return res.getDrawable(R.drawable.ic_memory_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_memory_white_disable_24px, getTheme());
                }
                case SingleAction.TOGGLE_NAV_LOCK: {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab == null) return null;
                    if (tab.isNavLock())
                        return res.getDrawable(R.drawable.ic_lock_outline_white_24px, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_lock_open_white_24px, getTheme());
                }
                case SingleAction.PAGE_INFO:
                    return res.getDrawable(R.drawable.ic_info_white_24dp, getTheme());
                case SingleAction.COPY_URL:
                    return res.getDrawable(R.drawable.ic_mode_edit_white_24dp, getTheme());
                case SingleAction.COPY_TITLE:
                    return res.getDrawable(R.drawable.ic_mode_edit_white_24dp, getTheme());
                case SingleAction.COPY_TITLE_URL:
                    return res.getDrawable(R.drawable.ic_mode_edit_white_24dp, getTheme());
                case SingleAction.TAB_HISTORY:
                    return res.getDrawable(R.drawable.ic_undo_white_24dp, getTheme());
                case SingleAction.MOUSE_POINTER:
                    return res.getDrawable(R.drawable.ic_mouse_white_24dp, getTheme());
                case SingleAction.FIND_ON_PAGE:
                    return res.getDrawable(R.drawable.ic_find_in_page_white_24px, getTheme());
                case SingleAction.SAVE_SCREENSHOT:
                    return res.getDrawable(R.drawable.ic_photo_white_24dp, getTheme());
                case SingleAction.SHARE_SCREENSHOT:
                    return res.getDrawable(R.drawable.ic_photo_white_24dp, getTheme());
                case SingleAction.SAVE_PAGE:
                    return res.getDrawable(R.drawable.ic_save_white_24dp, getTheme());
                case SingleAction.OPEN_URL:
                    return res.getDrawable(R.drawable.ic_book_white_24dp, getTheme());
                case SingleAction.TRANSLATE_PAGE:
                    return res.getDrawable(R.drawable.ic_g_translate_white_24px, getTheme());
                case SingleAction.NEW_TAB:
                    return res.getDrawable(R.drawable.ic_add_box_white_24dp, getTheme());
                case SingleAction.CLOSE_TAB:
                    return res.getDrawable(R.drawable.ic_minas_box_white_24dp, getTheme());
                case SingleAction.CLOSE_ALL:
                    return res.getDrawable(R.drawable.ic_minas_box_white_24dp, getTheme());
                case SingleAction.CLOSE_AUTO_SELECT:
                    return res.getDrawable(R.drawable.ic_minas_box_white_24dp, getTheme());
                case SingleAction.CLOSE_OTHERS:
                    return res.getDrawable(R.drawable.ic_minas_box_white_24dp, getTheme());
                case SingleAction.LEFT_TAB:
                    return res.getDrawable(R.drawable.ic_chevron_left_white_24dp, getTheme());
                case SingleAction.RIGHT_TAB:
                    return res.getDrawable(R.drawable.ic_chevron_right_white_24dp, getTheme());
                case SingleAction.SWAP_LEFT_TAB:
                    return res.getDrawable(R.drawable.ic_fast_rewind_white_24dp, getTheme());
                case SingleAction.SWAP_RIGHT_TAB:
                    return res.getDrawable(R.drawable.ic_fast_forward_white_24dp, getTheme());
                case SingleAction.TAB_LIST: {
                    Drawable base = res.getDrawable(R.drawable.ic_tab_white_24dp, getTheme());
                    Drawable text = new TabListActionTextDrawable(getApplicationContext(), mTabManager.size());
                    return new SimpleLayerDrawable(base, text);
                }
                case SingleAction.CLOSE_ALL_LEFT:
                    return res.getDrawable(R.drawable.ic_skip_previous_white_24dp, getTheme());
                case SingleAction.CLOSE_ALL_RIGHT:
                    return res.getDrawable(R.drawable.ic_skip_next_white_24dp, getTheme());
                case SingleAction.RESTORE_TAB:
                    return res.getDrawable(R.drawable.ic_redo_white_24dp, getTheme());
                case SingleAction.REPLICATE_TAB:
                    return res.getDrawable(R.drawable.ic_content_copy_white_24dp, getTheme());
                case SingleAction.SHOW_SEARCHBOX:
                    return res.getDrawable(R.drawable.ic_search_white_24dp, getTheme());
                case SingleAction.PASTE_SEARCHBOX:
                    return res.getDrawable(R.drawable.ic_content_paste_white_24dp, getTheme());
                case SingleAction.PASTE_GO:
                    return res.getDrawable(R.drawable.ic_content_paste_white_24dp, getTheme());
                case SingleAction.SHOW_BOOKMARK:
                    return res.getDrawable(R.drawable.ic_collections_bookmark_white_24dp, getTheme());
                case SingleAction.SHOW_HISTORY:
                    return res.getDrawable(R.drawable.ic_history_white_24dp, getTheme());
                case SingleAction.SHOW_DOWNLOADS:
                    return res.getDrawable(R.drawable.ic_file_download_white_24dp, getTheme());
                case SingleAction.SHOW_SETTINGS:
                    return res.getDrawable(R.drawable.ic_settings_white_24dp, getTheme());
                case SingleAction.OPEN_SPEED_DIAL:
                    return res.getDrawable(R.drawable.ic_speed_dial_white_24dp, getTheme());
                case SingleAction.ADD_BOOKMARK: {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab == null) return null;
                    if (BookmarkManager.getInstance(getApplicationContext()).isBookmarked(tab.getUrl()))
                        return res.getDrawable(R.drawable.ic_star_white_24px, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_star_border_white_24px, getTheme());
                }
                case SingleAction.ADD_SPEED_DIAL:
                    return res.getDrawable(R.drawable.ic_speed_dial_add_white_24dp, getTheme());
                case SingleAction.ADD_PATTERN:
                    return res.getDrawable(R.drawable.ic_pattern_add_white_24dp, getTheme());
                case SingleAction.ADD_TO_HOME:
                    return res.getDrawable(R.drawable.ic_add_to_home_white_24dp, getTheme());
                case SingleAction.SUB_GESTURE:
                    return res.getDrawable(R.drawable.ic_gesture_white_24dp, getTheme());
                case SingleAction.CLEAR_DATA:
                    return res.getDrawable(R.drawable.ic_delete_sweep_white_24px, getTheme());
                case SingleAction.SHOW_PROXY_SETTING:
                    return res.getDrawable(R.drawable.ic_import_export_white_24dp, getTheme());
                case SingleAction.ORIENTATION_SETTING:
                    return res.getDrawable(R.drawable.ic_stay_current_portrait_white_24dp, getTheme());
                case SingleAction.OPEN_LINK_SETTING:
                    return res.getDrawable(R.drawable.ic_link_white_24dp, getTheme());
                case SingleAction.USERAGENT_SETTING:
                    return res.getDrawable(R.drawable.ic_group_white_24dp, getTheme());
                case SingleAction.TEXTSIZE_SETTING:
                    return res.getDrawable(R.drawable.ic_format_size_white_24dp, getTheme());
                case SingleAction.USERJS_SETTING:
                    return res.getDrawable(R.drawable.ic_memory_white_24dp, getTheme());
                case SingleAction.WEB_ENCODE_SETTING:
                    return res.getDrawable(R.drawable.ic_format_shapes_white_24dp, getTheme());
                case SingleAction.DEFALUT_USERAGENT_SETTING:
                    return res.getDrawable(R.drawable.ic_group_white_24dp, getTheme());
                case SingleAction.RENDER_SETTING:
                    return res.getDrawable(R.drawable.ic_blur_linear_white_24dp, getTheme());
                case SingleAction.TOGGLE_VISIBLE_TAB:
                    return res.getDrawable(R.drawable.ic_remove_red_eye_white_24dp, getTheme());
                case SingleAction.TOGGLE_VISIBLE_URL:
                    return res.getDrawable(R.drawable.ic_remove_red_eye_white_24dp, getTheme());
                case SingleAction.TOGGLE_VISIBLE_PROGRESS:
                    return res.getDrawable(R.drawable.ic_remove_red_eye_white_24dp, getTheme());
                case SingleAction.TOGGLE_VISIBLE_CUSTOM:
                    return res.getDrawable(R.drawable.ic_remove_red_eye_white_24dp, getTheme());
                case SingleAction.TOGGLE_WEB_TITLEBAR:
                    return res.getDrawable(R.drawable.ic_web_asset_white_24dp, getTheme());
                case SingleAction.TOGGLE_WEB_GESTURE: {
                    if (webGestureOverlayView.isEnabled())
                        return res.getDrawable(R.drawable.ic_gesture_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_gesture_white_disable_24px, getTheme());
                }
                case SingleAction.TOGGLE_FLICK: {
                    if (AppData.flick_enable.get())
                        return res.getDrawable(R.drawable.ic_gesture_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_gesture_white_disable_24px, getTheme());
                }
                case SingleAction.TOGGLE_QUICK_CONTROL: {
                    if (getQuickControlEnabled())
                        return res.getDrawable(R.drawable.ic_pie_chart_outlined_white_24px, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_pie_chart_outlined_disable_white_24px, getTheme());
                }
                case SingleAction.TOGGLE_MULTI_FINGER_GESTURE: {
                    if (getMultiFingerGestureEnabled())
                        return res.getDrawable(R.drawable.ic_gesture_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_gesture_white_disable_24px, getTheme());
                }
                case SingleAction.TOGGLE_AD_BLOCK: {
                    if (adBlockController != null)
                        return res.getDrawable(R.drawable.ic_ad_block_enable_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_ad_block_disable_white_24dp, getTheme());
                }
                case SingleAction.OPEN_BLACK_LIST:
                    return res.getDrawable(R.drawable.ic_open_ad_block_black_24dp, getTheme());
                case SingleAction.OPEN_WHITE_LIST:
                    return res.getDrawable(R.drawable.ic_open_ad_block_white_24dp, getTheme());
                case SingleAction.OPEN_WHITE_PATE_LIST:
                    return res.getDrawable(R.drawable.ic_open_ad_block_white_page_24dp, getTheme());
                case SingleAction.ADD_WHITE_LIST_PAGE:
                    return res.getDrawable(R.drawable.ic_add_white_page_24dp, getTheme());
                case SingleAction.SHARE_WEB:
                    return res.getDrawable(R.drawable.ic_share_white_24dp, getTheme());
                case SingleAction.OPEN_OTHER:
                    return res.getDrawable(R.drawable.ic_public_white_24dp, getTheme());
                case SingleAction.START_ACTIVITY:
                    return ((StartActivitySingleAction) action).getIconDrawable(getApplicationContext());
                case SingleAction.TOGGLE_FULL_SCREEN:
                    return res.getDrawable(R.drawable.ic_fullscreen_white_24dp, getTheme());
                case SingleAction.OPEN_OPTIONS_MENU:
                    return res.getDrawable(R.drawable.ic_more_vert_white_24dp, getTheme());
                case SingleAction.CUSTOM_MENU:
                    return res.getDrawable(R.drawable.ic_more_vert_white_24dp, getTheme());
                case SingleAction.FINISH:
                    return res.getDrawable(R.drawable.ic_power_settings_white_24dp, getTheme());
                case SingleAction.MINIMIZE:
                    return res.getDrawable(R.drawable.ic_fullscreen_exit_white_24dp, getTheme());
                case SingleAction.CUSTOM_ACTION:
                    return getIcon(((CustomSingleAction) action).getAction());
                case SingleAction.VIBRATION:
                    return null;
                case SingleAction.TOAST:
                    return null;
                case SingleAction.PRIVATE:
                    if (AppData.private_mode.get())
                        return res.getDrawable(R.drawable.ic_private_white_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_private_white_disable_24dp, getTheme());
                case SingleAction.VIEW_SOURCE:
                    return res.getDrawable(R.drawable.ic_view_source_white_24dp, getTheme());
                case SingleAction.PRINT:
                    return res.getDrawable(R.drawable.ic_print_white_24dp, getTheme());
                case SingleAction.TAB_PINNING: {
                    MainTabData tab = mTabManager.getCurrentTabData();
                    if (tab == null) return null;
                    if (tab.isPinning())
                        return res.getDrawable(R.drawable.ic_pin_24dp, getTheme());
                    else
                        return res.getDrawable(R.drawable.ic_pin_disable_24dp, getTheme());
                }
                case SingleAction.ALL_ACTION:
                    return res.getDrawable(R.drawable.ic_list_white_24dp, getTheme());
                case SingleAction.READER_MODE:
                    return res.getDrawable(R.drawable.ic_chrome_reader_mode_white_24dp, getTheme());
                case SingleAction.READ_IT_LATER:
                    return res.getDrawable(R.drawable.ic_watch_later_white_24dp, getTheme());
                case SingleAction.READ_IT_LATER_LIST:
                    return res.getDrawable(R.drawable.ic_read_it_list_white_24px, getTheme());
                default:
                    Toast.makeText(getApplicationContext(), "Unknown action:" + action.id, Toast.LENGTH_LONG).show();
                    return null;
            }
        }
    }
}
