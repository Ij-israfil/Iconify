package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.installer.IconInstaller;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class IconPacks extends AppCompatActivity {

    private static final String AURORA_KEY = "IconifyComponentIPAS1.overlay";
    private static final String GRADICON_KEY = "IconifyComponentIPAS2.overlay";
    private static final String LORN_KEY = "IconifyComponentIPAS3.overlay";
    private static final String PLUMPY_KEY = "IconifyComponentIPAS4.overlay";

    private ViewGroup container;
    private LinearLayout spinner;
    LinearLayout[] Container;
    LinearLayout AuroraContainer, GradiconContainer, LornContainer, PlumpyContainer;
    Button Aurora_Enable, Aurora_Disable, Gradicon_Enable, Gradicon_Disable, Lorn_Enable, Lorn_Disable, Plumpy_Enable, Plumpy_Disable;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icon_packs);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Icon Pack");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Progressbar while enabling or disabling pack
        spinner = findViewById(R.id.progressBar_iconPack);

        // Don't show progressbar on opening page
        spinner.setVisibility(View.GONE);

        // Icon Pack list items
        container = (ViewGroup) findViewById(R.id.icon_packs_list);

        // Icon Pack add items in list
        addItem(R.id.iconPack_aurora_container, "Aurora", "Dual tone linear icon pack", R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location, R.id.iconPack_aurora_enable, R.id.iconPack_aurora_disable);
        addItem(R.id.iconPack_gradicon_container, "Gradicon", "Gradient shaded filled icon pack", R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location, R.id.iconPack_gradicon_enable, R.id.iconPack_gradicon_disable);
        addItem(R.id.iconPack_lorn_container, "Lorn", "Thick linear icon pack", R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location, R.id.iconPack_lorn_enable, R.id.iconPack_lorn_disable);
        addItem(R.id.iconPack_plumpy_container, "Plumpy", "Dual tone filled icon pack", R.drawable.preview_plumpy_wifi, R.drawable.preview_plumpy_signal, R.drawable.preview_plumpy_airplane, R.drawable.preview_plumpy_location, R.id.iconPack_plumpy_enable, R.id.iconPack_plumpy_disable);

        // Declaration of Aurora
        AuroraContainer = findViewById(R.id.iconPack_aurora_container);
        Aurora_Enable = findViewById(R.id.iconPack_aurora_enable);
        Aurora_Disable = findViewById(R.id.iconPack_aurora_disable);

        // Declaration of Gradicon
        GradiconContainer = findViewById(R.id.iconPack_gradicon_container);
        Gradicon_Enable = findViewById(R.id.iconPack_gradicon_enable);
        Gradicon_Disable = findViewById(R.id.iconPack_gradicon_disable);

        // Declaration of Lorn
        LornContainer = findViewById(R.id.iconPack_lorn_container);
        Lorn_Enable = findViewById(R.id.iconPack_lorn_enable);
        Lorn_Disable = findViewById(R.id.iconPack_lorn_disable);

        // Declaration of Plumpy
        PlumpyContainer = findViewById(R.id.iconPack_plumpy_container);
        Plumpy_Enable = findViewById(R.id.iconPack_plumpy_enable);
        Plumpy_Disable = findViewById(R.id.iconPack_plumpy_disable);

        // List of Icon Pack
        Container = new LinearLayout[]{AuroraContainer, GradiconContainer, LornContainer, PlumpyContainer};

        // Enable onClick event
        enableOnClickListener(AuroraContainer, Aurora_Enable, Aurora_Disable, AURORA_KEY, 1);
        enableOnClickListener(GradiconContainer, Gradicon_Enable, Gradicon_Disable, GRADICON_KEY, 2);
        enableOnClickListener(LornContainer, Lorn_Enable, Lorn_Disable, LORN_KEY, 3);
        enableOnClickListener(PlumpyContainer, Plumpy_Enable, Plumpy_Disable, PLUMPY_KEY, 4);

        refreshBackground();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {
        for (LinearLayout linearLayout : Container) {
            if (!(linearLayout == layout)) {
                if (linearLayout == AuroraContainer) {
                    Aurora_Enable.setVisibility(View.GONE);
                    Aurora_Disable.setVisibility(View.GONE);
                } else if (linearLayout == GradiconContainer) {
                    Gradicon_Enable.setVisibility(View.GONE);
                    Gradicon_Disable.setVisibility(View.GONE);
                } else if (linearLayout == LornContainer) {
                    Lorn_Enable.setVisibility(View.GONE);
                    Lorn_Disable.setVisibility(View.GONE);
                } else if (linearLayout == PlumpyContainer) {
                    Plumpy_Enable.setVisibility(View.GONE);
                    Plumpy_Disable.setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        checkIfApplied(AuroraContainer, 1);
        checkIfApplied(GradiconContainer, 2);
        checkIfApplied(LornContainer, 3);
        checkIfApplied(PlumpyContainer, 4);
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index) {

        // Set onClick operation for options in list
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                if (!PrefConfig.loadPrefBool(getApplicationContext(), key)) {
                    disable.setVisibility(View.GONE);
                    if (enable.getVisibility() == View.VISIBLE)
                        enable.setVisibility(View.GONE);
                    else
                        enable.setVisibility(View.VISIBLE);
                } else {
                    enable.setVisibility(View.GONE);
                    if (disable.getVisibility() == View.VISIBLE)
                        disable.setVisibility(View.GONE);
                    else
                        disable.setVisibility(View.VISIBLE);
                }
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLayout(layout);
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        disable_others(key);
                        IconInstaller.install_pack(index);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                PrefConfig.savePrefBool(getApplicationContext(), key, true);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Change background to selected
                        background(layout.getId(), R.drawable.container_selected);
                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();
                        Toast.makeText(getApplicationContext(), "Applied", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show spinner
                spinner.setVisibility(View.VISIBLE);
                // Block touch
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        IconInstaller.disable_pack(index);
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                PrefConfig.savePrefBool(getApplicationContext(), key, false);
                // Wait 1 second
                spinner.postDelayed(new Runnable() {
                    public void run() {
                        // Hide spinner
                        spinner.setVisibility(View.GONE);
                        // Unblock touch
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        // Change background to selected
                        background(layout.getId(), R.drawable.container);
                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();
                        Toast.makeText(getApplicationContext(), "Disabled", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });
    }

    // Function to disable other packs if one is applied
    private void disable_others(String pack) {
        if (Objects.equals(pack, AURORA_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), GRADICON_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LORN_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), PLUMPY_KEY, false);
        } else if (Objects.equals(pack, GRADICON_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), AURORA_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LORN_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), PLUMPY_KEY, false);
        } else if (Objects.equals(pack, LORN_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), AURORA_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), GRADICON_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), PLUMPY_KEY, false);
        } else if (Objects.equals(pack, PLUMPY_KEY)) {
            PrefConfig.savePrefBool(getApplicationContext(), AURORA_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), GRADICON_KEY, false);
            PrefConfig.savePrefBool(getApplicationContext(), LORN_KEY, false);
        }
    }

    // Function to change applied pack's bg
    private void checkIfApplied(LinearLayout layout, int icon) {
        if (PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentIPAS" + icon + ".overlay") && PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentIPSUI" + icon + ".overlay")) {
            background(layout.getId(), R.drawable.container_selected);
        } else {
            background(layout.getId(), R.drawable.container);
        }
    }

    // Function to add border for installed pack
    private void background(int id, int drawable) {
        LinearLayout layout = findViewById(id);
        layout.setBackground(ContextCompat.getDrawable(this, drawable));
    }

    // Function to add new item in list
    private void addItem(int id, String title, String desc, int preview1, int preview2, int preview3, int preview4, int enableid, int disableid) {
        View list = LayoutInflater.from(this).inflate(R.layout.list_option_iconpack, container, false);

        TextView name = list.findViewById(R.id.list_title_iconpack);
        TextView info = list.findViewById(R.id.list_desc_iconpack);
        ImageView ic1 = list.findViewById(R.id.list_preview1_iconpack);
        ImageView ic2 = list.findViewById(R.id.list_preview2_iconpack);
        ImageView ic3 = list.findViewById(R.id.list_preview3_iconpack);
        ImageView ic4 = list.findViewById(R.id.list_preview4_iconpack);
        Button enable = list.findViewById(R.id.list_button_enable_iconpack);
        Button disable = list.findViewById(R.id.list_button_disable_iconpack);

        list.setId(id);
        name.setText(title);
        info.setText(desc);

        ic1.setImageResource(preview1);
        ic2.setImageResource(preview2);
        ic3.setImageResource(preview3);
        ic4.setImageResource(preview4);

        enable.setId(enableid);
        disable.setId(disableid);

        container.addView(list);
    }
}