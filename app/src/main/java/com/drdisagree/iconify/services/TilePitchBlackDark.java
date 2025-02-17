package com.drdisagree.iconify.services;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

public class TilePitchBlackDark extends TileService {

    private boolean isPitchBlackEnabled = Prefs.getBoolean("IconifyComponentQSPBD.overlay");

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile pitchBlackTile = getQsTile();
        pitchBlackTile.setState(isPitchBlackEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        pitchBlackTile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (isPitchBlackEnabled) {
            OverlayUtil.disableOverlay("IconifyComponentQSPBD.overlay");
        } else {
            OverlayUtil.enableOverlay("IconifyComponentQSPBD.overlay");
        }

        isPitchBlackEnabled = !isPitchBlackEnabled;

        Tile pitchBlackTile = getQsTile();
        pitchBlackTile.setState(isPitchBlackEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        pitchBlackTile.setLabel(getResources().getString(R.string.pitch_black_dark_title));
        pitchBlackTile.setContentDescription(isPitchBlackEnabled ? getResources().getString(R.string.general_on) : getResources().getString(R.string.general_off));
        pitchBlackTile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
