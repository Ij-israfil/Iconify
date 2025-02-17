package com.drdisagree.iconify.xposed.mods;

/* Modified from AOSPMods
 * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/systemui/QSThemeManager.java
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.utils.SettingsLibUtils.getColorAttr;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getFloatField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.xposed.HookEntry;
import com.drdisagree.iconify.xposed.ModPack;
import com.drdisagree.iconify.xposed.utils.Helpers;
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils;
import com.drdisagree.iconify.xposed.utils.SystemUtil;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("RedundantThrows")
public class QSBlackTheme extends ModPack {

    public static final String listenPackage = SYSTEMUI_PACKAGE;
    public static final int STATE_ACTIVE = 2;
    private static final String TAG = "Iconify - QSBlackTheme: ";
    private static boolean blackQSHeaderEnabled = false;
    private Object mBehindColors;
    private boolean wasDark;
    private Integer colorText = null;
    private Drawable darkFooterShape = null;
    private Object mClockViewQSHeader = null;

    public QSBlackTheme(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;

        darkFooterShape = makeFooterShape();
        wasDark = getIsDark();
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        blackQSHeaderEnabled = Xprefs.getBoolean(BLACK_QSPANEL, false);

        setBlackQSHeader(blackQSHeaderEnabled);
    }

    public void setBlackQSHeader(boolean state) {
        if (blackQSHeaderEnabled != state) {
            blackQSHeaderEnabled = state;

            try {
                applyOverlays(true);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> QSTileViewImplClass = findClass("com.android.systemui.qs.tileimpl.QSTileViewImpl", lpparam.classLoader);
        Class<?> FragmentHostManagerClass = findClass("com.android.systemui.fragments.FragmentHostManager", lpparam.classLoader);
        Class<?> ScrimControllerClass = findClass("com.android.systemui.statusbar.phone.ScrimController", lpparam.classLoader);
        Class<?> GradientColorsClass = findClass("com.android.internal.colorextraction.ColorExtractor$GradientColors", lpparam.classLoader);
        Class<?> QSPanelControllerClass = findClass("com.android.systemui.qs.QSPanelController", lpparam.classLoader);
        Class<?> InterestingConfigChangesClass = findClass("com.android.settingslib.applications.InterestingConfigChanges", lpparam.classLoader);
        Class<?> ScrimStateEnum = findClass("com.android.systemui.statusbar.phone.ScrimState", lpparam.classLoader);
        Class<?> QSIconViewImplClass = findClass("com.android.systemui.qs.tileimpl.QSIconViewImpl", lpparam.classLoader);
        Class<?> CentralSurfacesImplClass = findClass("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);
        Class<?> ClockClass = findClass("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader);
        Class<?> QuickStatusBarHeaderClass = findClass("com.android.systemui.qs.QuickStatusBarHeader", lpparam.classLoader);
        SettingsLibUtils.init(lpparam.classLoader);

        hookAllConstructors(QSPanelControllerClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                calculateColors();
            }
        });

        try { //QPR1
            Class<?> QSSecurityFooterClass = findClass("com.android.systemui.qs.QSSecurityFooter", lpparam.classLoader);
            Class<?> QSFgsManagerFooterClass = findClass("com.android.systemui.qs.QSFgsManagerFooter", lpparam.classLoader);
            Class<?> FooterActionsControllerClass = findClass("com.android.systemui.qs.FooterActionsController", lpparam.classLoader);

            hookAllConstructors(QSFgsManagerFooterClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!wasDark && blackQSHeaderEnabled) {
                        ((View) getObjectField(param.thisObject, "mNumberContainer")).getBackground().setTint(colorText);
                        ((View) getObjectField(param.thisObject, "mTextContainer")).setBackground(darkFooterShape.getConstantState().newDrawable().mutate()); //original has to be copied to new object otherwise will get affected by view changes
                    }
                }
            });

            hookAllConstructors(QSSecurityFooterClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!wasDark && blackQSHeaderEnabled) {
                        ((View) getObjectField(param.thisObject, "mView")).setBackground(darkFooterShape.getConstantState().newDrawable().mutate());
                    }
                }
            });

            hookAllConstructors(FooterActionsControllerClass, new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!wasDark && blackQSHeaderEnabled) {
                        Resources res = mContext.getResources();
                        ViewGroup view = (ViewGroup) param.args[0];

                        View settings_button_container = view.findViewById(res.getIdentifier("settings_button_container", "id", mContext.getPackageName()));
                        ((LinearLayout.LayoutParams) ((ViewGroup) settings_button_container.getParent()).getLayoutParams()).setMarginEnd(0);
                    }
                }
            });

        } catch (Throwable throwable) { //QPR2&3
            //QPR3
            Class<?> ShadeHeaderControllerClass = findClassIfExists("com.android.systemui.shade.ShadeHeaderController", lpparam.classLoader);
            //QPR2
            if (ShadeHeaderControllerClass == null)
                ShadeHeaderControllerClass = findClass("com.android.systemui.shade.LargeScreenShadeHeaderController", lpparam.classLoader);
            Class<?> QSContainerImplClass = findClass("com.android.systemui.qs.QSContainerImpl", lpparam.classLoader);

            hookAllMethods(ShadeHeaderControllerClass, "onInit", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View mView = (View) getObjectField(param.thisObject, "mView");
                    Object iconManager = getObjectField(param.thisObject, "iconManager");
                    Object batteryIcon = getObjectField(param.thisObject, "batteryIcon");
                    Object configurationControllerListener = getObjectField(param.thisObject, "configurationControllerListener");
                    hookAllMethods(configurationControllerListener.getClass(), "onConfigChanged", new XC_MethodHook() {
                        @SuppressLint("DiscouragedApi")
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!blackQSHeaderEnabled) return;

                            int textColor = Color.WHITE;

                            try {
                                ((TextView) mView.findViewById(mContext.getResources().getIdentifier("clock", "id", mContext.getPackageName()))).setTextColor(textColor);
                                ((TextView) mView.findViewById(mContext.getResources().getIdentifier("date", "id", mContext.getPackageName()))).setTextColor(textColor);
                            } catch (Throwable throwabl) {
                                log(TAG + throwabl);
                            }

                            callMethod(iconManager, "setTint", textColor);

                            for (int i = 1; i <= 3; i++) {
                                String id = String.format("carrier%s", i);

                                ((TextView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mCarrierText")).setTextColor(textColor);
                                ((ImageView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mMobileSignal")).setImageTintList(ColorStateList.valueOf(textColor));
                                ((ImageView) getObjectField(mView.findViewById(mContext.getResources().getIdentifier(id, "id", mContext.getPackageName())), "mMobileRoaming")).setImageTintList(ColorStateList.valueOf(textColor));
                            }

                            callMethod(batteryIcon, "updateColors", textColor, textColor, textColor);
                        }
                    });
                }
            });

            hookAllMethods(QSContainerImplClass, "updateResources", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!wasDark && blackQSHeaderEnabled) {
                        Resources res = mContext.getResources();
                        ViewGroup view = (ViewGroup) param.thisObject;

                        View settings_button_container = view.findViewById(res.getIdentifier("settings_button_container", "id", mContext.getPackageName()));
                        ImageView icon = settings_button_container.findViewById(res.getIdentifier("icon", "id", mContext.getPackageName()));
                        icon.setImageTintList(ColorStateList.valueOf(Color.WHITE));

                        ((FrameLayout.LayoutParams) ((ViewGroup) settings_button_container.getParent()).getLayoutParams()).setMarginEnd(0);

                        ViewGroup parent = (ViewGroup) settings_button_container.getParent();
                        for (int i = 0; i < 3; i++) //Security + Foreground services containers
                        {
                            parent.getChildAt(i).setBackground(darkFooterShape.getConstantState().newDrawable().mutate());
                        }
                    }
                }
            });
        }

        hookAllMethods(QSIconViewImplClass, "updateIcon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled && !getIsDark() && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                    ((ImageView) param.args[0]).setImageTintList(ColorStateList.valueOf(colorText));
                }
            }
        });

        hookAllMethods(QSIconViewImplClass, "setIcon", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled && !getIsDark()) {
                    if (param.args[0] instanceof ImageView && getIntField(param.args[1], "state") == STATE_ACTIVE) {
                        setObjectField(param.thisObject, "mTint", colorText);
                    }
                }
            }
        });

        // White QS Clock bug
        hookAllMethods(QuickStatusBarHeaderClass, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    mClockViewQSHeader = getObjectField(param.thisObject, "mClockView");
                } catch (Throwable ignored) {
                }
            }
        });

        // White QS Clock bug
        hookAllMethods(ClockClass, "onColorsChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (blackQSHeaderEnabled && !SystemUtil.isDarkMode() && mClockViewQSHeader != null) {
                    ((TextView) mClockViewQSHeader).setTextColor(Color.WHITE);
                }
            }
        });

        hookAllConstructors(CentralSurfacesImplClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                applyOverlays(true);
            }
        });

        hookAllConstructors(QSTileViewImplClass, new XC_MethodHook() {
            @Override
            @SuppressLint("DiscouragedApi")
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled || wasDark) return;

                setObjectField(param.thisObject, "colorLabelActive", Color.WHITE);
                setObjectField(param.thisObject, "colorSecondaryLabelActive", Color.WHITE);

                ViewGroup sideView = (ViewGroup) getObjectField(param.thisObject, "sideView");
                ImageView customDrawable = sideView.findViewById(mContext.getResources().getIdentifier("customDrawable", "id", mContext.getPackageName()));
                customDrawable.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                ImageView chevron = sideView.findViewById(mContext.getResources().getIdentifier("chevron", "id", mContext.getPackageName()));
                chevron.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            }
        });

        hookAllMethods(QSIconViewImplClass, "getIconColorForState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!wasDark && blackQSHeaderEnabled && ((boolean) param.args[1])) {
                    param.setResult(Color.WHITE);
                }
            }
        });

        hookAllMethods(CentralSurfacesImplClass, "updateTheme", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                applyOverlays(false);
            }
        });

        try {
            mBehindColors = GradientColorsClass.newInstance();
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }

        hookAllMethods(ScrimControllerClass, "updateScrims", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled) return;

                Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                boolean mBlankScreen = (boolean) getObjectField(param.thisObject, "mBlankScreen");
                float alpha = getFloatField(mScrimBehind, "mViewAlpha");
                boolean animateBehindScrim = alpha != 0 && !mBlankScreen;

                callMethod(mScrimBehind, "setColors", mBehindColors, animateBehindScrim);
            }
        });

        hookAllMethods(ScrimControllerClass, "updateThemeColors", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                calculateColors();
            }
        });

        hookAllMethods(ScrimControllerClass, "updateThemeColors", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled) return;

                @SuppressLint("DiscouragedApi") ColorStateList states = getColorAttr(mContext.getResources().getIdentifier("android:attr/colorBackgroundFloating", "attr", listenPackage), mContext);

                int surfaceBackground = states.getDefaultColor();

                @SuppressLint("DiscouragedApi") ColorStateList accentStates = getColorAttr(mContext.getResources().getIdentifier("colorAccent", "attr", "android"), mContext);
                int accent = accentStates.getDefaultColor();

                callMethod(mBehindColors, "setMainColor", surfaceBackground);
                callMethod(mBehindColors, "setSecondaryColor", accent);

                double contrast = ColorUtils.calculateContrast((int) callMethod(mBehindColors, "getMainColor"), Color.WHITE);

                callMethod(mBehindColors, "setSupportsDarkText", contrast > 4.5);
            }
        });

        hookAllMethods(ScrimControllerClass, "applyState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!blackQSHeaderEnabled) return;

                boolean mClipsQsScrim = (boolean) getObjectField(param.thisObject, "mClipsQsScrim");
                if (mClipsQsScrim) {
                    setObjectField(param.thisObject, "mBehindTint", Color.BLACK);
                }
            }
        });

        Object[] constants = ScrimStateEnum.getEnumConstants();
        for (Object constant : constants) {
            String enumVal = constant.toString();
            switch (enumVal) {
                case "KEYGUARD":
                    hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!blackQSHeaderEnabled) return;
                            boolean mClipQsScrim = (boolean) getObjectField(param.thisObject, "mClipQsScrim");
                            if (mClipQsScrim) {
                                Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                                int mTintColor = getIntField(mScrimBehind, "mTintColor");
                                if (mTintColor != Color.BLACK) {
                                    setObjectField(mScrimBehind, "mTintColor", Color.BLACK);
                                    callMethod(mScrimBehind, "updateColorWithTint", false);
                                }

                                callMethod(mScrimBehind, "setViewAlpha", 1f);
                            }
                        }
                    });
                    break;
                case "BOUNCER":
                    hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!blackQSHeaderEnabled) return;

                            setObjectField(param.thisObject, "mBehindTint", Color.BLACK);
                        }
                    });
                    break;
                case "SHADE_LOCKED":
                    hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!blackQSHeaderEnabled) return;

                            setObjectField(param.thisObject, "mBehindTint", Color.BLACK);
                            boolean mClipQsScrim = (boolean) getObjectField(param.thisObject, "mClipQsScrim");
                            if (mClipQsScrim) {
                                Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                                int mTintColor = getIntField(mScrimBehind, "mTintColor");
                                if (mTintColor != Color.BLACK) {
                                    setObjectField(mScrimBehind, "mTintColor", Color.BLACK);
                                    callMethod(mScrimBehind, "updateColorWithTint", false);
                                }

                                callMethod(mScrimBehind, "setViewAlpha", 1f);
                            }
                        }
                    });
                    hookAllMethods(constant.getClass(), "getBehindTint", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!blackQSHeaderEnabled) return;
                            param.setResult(Color.BLACK);
                        }
                    });
                    break;

                case "UNLOCKED":
                    hookAllMethods(constant.getClass(), "prepare", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!blackQSHeaderEnabled) return;

                            setObjectField(param.thisObject, "mBehindTint", Color.BLACK);

                            Object mScrimBehind = getObjectField(param.thisObject, "mScrimBehind");
                            int mTintColor = getIntField(mScrimBehind, "mTintColor");
                            if (mTintColor != Color.BLACK) {
                                setObjectField(mScrimBehind, "mTintColor", Color.BLACK);
                                callMethod(mScrimBehind, "updateColorWithTint", false);
                            }
                            callMethod(mScrimBehind, "setViewAlpha", 1f);
                        }
                    });
                    break;
            }
        }

        hookAllConstructors(FragmentHostManagerClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                setObjectField(param.thisObject, "mConfigChanges", InterestingConfigChangesClass.getDeclaredConstructor(int.class).newInstance(0x40000000 | 0x0004 | 0x0100 | 0x80000000 | 0x0200));
            }
        });

    }

    private void applyOverlays(boolean force) throws Throwable {
        boolean isDark = getIsDark();

        if (isDark == wasDark && !force) return;
        wasDark = isDark;

        calculateColors();

        Helpers.disableOverlay("IconifyComponentQSLT.overlay");

        Thread.sleep(50);

        if (blackQSHeaderEnabled && !isDark) {
            Helpers.enableOverlay("IconifyComponentQSLT.overlay");
        }
    }

    @SuppressLint("DiscouragedApi")
    private void calculateColors() {
        if (!blackQSHeaderEnabled || wasDark) return;

        Resources res = mContext.getResources();
        colorText = res.getColor(res.getIdentifier("android:color/system_neutral1_10", "color", listenPackage), mContext.getTheme());
        Integer colorInactive = res.getColor(res.getIdentifier("android:color/system_neutral1_900", "color", listenPackage), mContext.getTheme());

        darkFooterShape.setTint(colorInactive);
    }

    private boolean getIsDark() {
        return (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    private Drawable makeFooterShape() {
        @SuppressLint("DiscouragedApi") int radius = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("qs_security_footer_corner_radius", "dimen", mContext.getPackageName()));
        float[] radiusF = new float[8];
        for (int i = 0; i < 8; i++) {
            radiusF[i] = radius;
        }
        final ShapeDrawable result = new ShapeDrawable(new RoundRectShape(radiusF, null, null));
        result.getPaint().setStyle(Paint.Style.FILL);
        return result;
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !HookEntry.isChildProcess && Build.VERSION.SDK_INT >= 33;
    }
}