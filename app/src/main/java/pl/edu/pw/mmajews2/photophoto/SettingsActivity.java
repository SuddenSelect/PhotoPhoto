package pl.edu.pw.mmajews2.photophoto;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Size;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    public static Size[] MAIN_RESOLUTIONS = null;
    public static Size[] HIDDEN_RESOLUTIONS = null;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || PicturePreferenceFragment.class.getName().equals(fragmentName)
                || MetadataPreferenceFragment.class.getName().equals(fragmentName)
                || EncryptionPreferenceFragment.class.getName().equals(fragmentName);
    }


    public static class PicturePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_picture);
            setHasOptionsMenu(true);

            //Dynamic resolution listing
            ListPreference mainPictureResolution = (ListPreference) findPreference("pl.edu.pw.mmajews2.photophoto.picture.main.resolution");
            fillResolutionList(mainPictureResolution, MAIN_RESOLUTIONS, Integer.MAX_VALUE);

            ListPreference hiddenPictureResolution = (ListPreference) findPreference("pl.edu.pw.mmajews2.photophoto.picture.hidden.resolution");
            Size selected = Size.parseSize(mainPictureResolution.getValue());
            fillResolutionList(hiddenPictureResolution, HIDDEN_RESOLUTIONS, selected.getHeight()*selected.getWidth());

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pl.edu.pw.mmajews2.photophoto.picture.main.resolution"));
            bindPreferenceSummaryToValue(findPreference("pl.edu.pw.mmajews2.photophoto.picture.hidden.resolution"));
            bindPreferenceSummaryToValue(findPreference("pl.edu.pw.mmajews2.photophoto.picture.channel_bits"));
            bindPreferenceSummaryToValue(findPreference("pl.edu.pw.mmajews2.photophoto.picture.file_prefix"));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private void fillResolutionList(ListPreference listPreference, Size[] resolutions, int limit){
            ArrayList<CharSequence> viableSizes = new ArrayList<>(resolutions.length);
            String currentChoice = listPreference.getValue();

            for (Size res : resolutions) {
//                if(res.getHeight() * res.getWidth() * 4 <= limit*2){
                    viableSizes.add(res.toString());
//                }
            }

            viableSizes.trimToSize();
            CharSequence entries[] = new String[viableSizes.size()];
            entries = viableSizes.toArray(entries);

            listPreference.setEntries(entries);
            listPreference.setDefaultValue(viableSizes.get(0));
            if(currentChoice == null) {
                listPreference.setValue(viableSizes.get(0).toString());
                listPreference.setSummary(viableSizes.get(0).toString());
            }else {
                listPreference.setValue(currentChoice);
                listPreference.setSummary(currentChoice);
            }
            listPreference.setEntryValues(entries);
        }

    }

    public static class MetadataPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_metadata);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pl.edu.pw.mmajews2.photophoto.metadata.marker.text"));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class EncryptionPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_encryption);
            setHasOptionsMenu(true);

//            findPreference("encryption_enabled_switch").setEnabled(false);//TODO
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


    }


}
