package org.openstreetmap.josm.plugins.strava.heatmap.preference;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.I18n;

import javax.swing.*;
import java.awt.*;

import static org.openstreetmap.josm.tools.I18n.tr;


/**
 * Builds the plugin preferences page where the user can set the Strava account details.
 */
public class StravaHeatmapPreferenceSetting extends DefaultTabPreferenceSetting {


    private JTextField email = new JTextField(20);
    private JPasswordField password = new JPasswordField(20);


    public StravaHeatmapPreferenceSetting() {
        super("strava_heatmap.png", I18n.tr("Strava Heatmap"),
                I18n.tr("This plugin provides access to the High-resolution Strava heatmaps (zoom level >12)"
                        +" that require to be authenticated with a Strava account.<br/>"
                        +" The plugin stores Strava account details (email and password) and transparently manages the authentication process.<br/><br/>"
                        +" Set your Strava account details in the fields below and open the default Strava heatmap layers defined in the menu Imagery > Imagery Preference")
        );
    }

    @Override
    public void addGui(final PreferenceTabbedPane gui) {

        JPanel stravaHeatmapMast = gui.createPreferenceTab(this);

        JPanel stravaHeatmap = new JPanel(new GridBagLayout());
        stravaHeatmap.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        email.setText(Config.getPref().get("strava.heatmap.email"));
        email.setToolTipText(tr(I18n.tr("<html>Email address linked to a Strava account</html>")));
        JLabel jLabelEmail = new JLabel(tr("Email"));
        stravaHeatmap.add(jLabelEmail, GBC.eop().insets(0, 0, 0, 0));
        stravaHeatmap.add(email, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        password.setText(Config.getPref().get("strava.heatmap.password"));
        password.setToolTipText(tr(I18n.tr("<html>Strava account password</html>")));
        JLabel jLabelPassword = new JLabel(tr("Password"));
        stravaHeatmap.add(jLabelPassword, GBC.eop().insets(0, 0, 0, 0));
        stravaHeatmap.add(password, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        stravaHeatmap.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
        JScrollPane scrollpane = new JScrollPane(stravaHeatmap);
        scrollpane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        stravaHeatmapMast.add(scrollpane, GBC.eol().fill(GBC.BOTH));

    }

    @Override
    public boolean ok() {
        Config.getPref().put("strava.heatmap.email", email.getText());
        Config.getPref().put("strava.heatmap.password", new String(password.getPassword()));
        return false;
    }
}
