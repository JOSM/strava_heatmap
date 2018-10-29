// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.strava.heatmap;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.strava.heatmap.layer.StravaHeatmapLayerProcessor;
import org.openstreetmap.josm.plugins.strava.heatmap.preference.StravaHeatmapPreferenceSetting;

/**
 * Plugin providing access to the high-resolution Strave heatmap.
 * The high-resolution heatmap requires to be authenticated with a valid Strava account.
 * When a layer is opened, the plugin checks whether it is a default Strava layer and add the required credentials.
 *
 */
public class StravaHeatmapPlugin extends Plugin implements LayerManager.LayerChangeListener {

    private StravaHeatmapLayerProcessor stravaHeatmapProcessor = new StravaHeatmapLayerProcessor();

    /**
     * Initializes the plugin.
     *
     * @param info Context information about the plugin.
     */
    public StravaHeatmapPlugin(PluginInformation info) {
        super(info);
        MainApplication.getLayerManager().addLayerChangeListener(this);
    }


    @Override
    public PreferenceSetting getPreferenceSetting() {
        PreferenceSetting preferenceSetting = new StravaHeatmapPreferenceSetting();
        return preferenceSetting;
    }

    @Override
    public void layerAdded(LayerManager.LayerAddEvent layerAddEvent) {
        Layer layer = layerAddEvent.getAddedLayer();
        stravaHeatmapProcessor.processLayer(layer);
    }

    @Override
    public void layerRemoving(LayerManager.LayerRemoveEvent layerRemoveEvent) {
    }

    @Override
    public void layerOrderChanged(LayerManager.LayerOrderChangeEvent layerOrderChangeEvent) {
    }
}
