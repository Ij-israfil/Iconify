package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.utils.helpers.Logger.writeLog;

import android.util.Log;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helpers.BinaryInstaller;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.Objects;

public class SettingsIconsCompiler {

    private static final String TAG = "SettingsIconsCompiler";
    private static final String[] packages = new String[]{"com.android.settings", "com.google.android.apps.wellbeing", "com.google.android.gms"};
    private static int mIconSet = 1, mIconBg = 1;

    public static boolean buildOverlay(int iconSet, int iconBg, String resources) throws IOException {
        mIconSet = iconSet;
        mIconBg = iconBg;

        preExecute();
        moveOverlaysToCache();

        for (int i = 0; i < packages.length; i++) {
            String overlay_name = "SIP" + (i + 1);

            // Create AndroidManifest.xml
            if (createManifest(overlay_name, packages[i], Resources.TEMP_CACHE_DIR + "/" + packages[i] + "/" + overlay_name)) {
                Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Write resources
            if (!Objects.equals(resources, "")) {
                if (writeResources(Resources.TEMP_CACHE_DIR + "/" + packages[i] + "/" + overlay_name, resources)) {
                    Log.e(TAG, "Failed to write resource for " + overlay_name + "! Exiting...");
                    postExecute(true);
                    return true;
                }
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(Resources.TEMP_CACHE_DIR + "/" + packages[i] + "/" + overlay_name)) {
                Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
                postExecute(true);
                return true;
            }

            // ZipAlign the APK
            if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/" + overlay_name + "-unsigned-unaligned.apk")) {
                Log.e(TAG, "Failed to align " + overlay_name + "-unsigned-unaligned.apk! Exiting...");
                postExecute(true);
                return true;
            }

            // Sign the APK
            if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/" + overlay_name + "-unsigned.apk")) {
                Log.e(TAG, "Failed to sign " + overlay_name + "-unsigned.apk! Exiting...");
                postExecute(true);
                return true;
            }
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Create symbolic link
        BinaryInstaller.symLinkBinaries();

        // Clean data directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();

        // Extract overlay from assets
        for (String aPackage : packages)
            FileUtil.copyAssets("CompileOnDemand/" + aPackage + "/SIP" + mIconSet);

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();
        for (String aPackages : packages)
            Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/" + aPackages + "/").exec();

        // Disable the overlay in case it is already enabled
        for (int i = 1; i <= packages.length; i++)
            OverlayUtil.disableOverlay("IconifyComponentSIP" + i + ".overlay");
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            for (int i = 1; i <= packages.length; i++) {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentSIP" + i + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponentSIP" + i + ".apk").exec();
                RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponentSIP" + i + ".apk");
            }

            SystemUtil.mountRW();
            for (int i = 1; i <= 3; i++) {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentSIP" + i + ".apk " + "/system/product/overlay/IconifyComponentSIP" + i + ".apk").exec();
                RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentSIP" + i + ".apk");
            }
            SystemUtil.mountRO();

            for (int i = 1; i <= 3; i++) {
                OverlayUtil.enableOverlay("IconifyComponentSIP" + i + ".overlay");
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();
    }

    private static void moveOverlaysToCache() {
        Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + packages[0] + "/" + "SIP" + mIconSet + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1\"").exec().isSuccess();
        Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + packages[1] + "/" + "SIP" + mIconSet + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2\"").exec().isSuccess();
        Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + packages[2] + "/" + "SIP" + mIconSet + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3\"").exec().isSuccess();

        if (mIconBg == 1) {
            Shell.cmd("rm -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable\"", "cp -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable-night\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable\"").exec();
            Shell.cmd("rm -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable-anydpi\"", "cp -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable-night-anydpi\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable-anydpi\"").exec();

            Shell.cmd("rm -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable\"", "cp -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable-night\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable\"").exec();
            Shell.cmd("rm -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable-anydpi\"", "cp -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable-night-anydpi\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable-anydpi\"").exec();

            Shell.cmd("rm -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable\"", "cp -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable-night\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable\"").exec();
            Shell.cmd("rm -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable-anydpi\"", "cp -rf \"" + Resources.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable-night-anydpi\" \"" + Resources.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable-anydpi\"").exec();
        }
    }

    private static boolean createManifest(String overlayName, String pkgName, String source) {
        Shell.Result result = Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + overlayName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + pkgName + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + pkgName);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + pkgName + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + pkgName, result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean writeResources(String source, String resources) {
        Shell.Result result = Shell.cmd("rm -rf " + source + "/res/values/Iconify.xml", "printf '" + resources + "' > " + source + "/res/values/Iconify.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - WriteResources", "Successfully written resources for SettingsIcons");
        else {
            Log.e(TAG + " - WriteResources", "Failed to write resources for SettingsIcons" + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - WriteResources", "Failed to write resources for SettingsIcons", result.getOut());
        }

        return !result.isSuccess();
    }
}
