package barqsoft.footballscores;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;

import barqsoft.footballscores.network.ApiError;
import barqsoft.footballscores.realm.League;
import barqsoft.footballscores.realm.Season;
import barqsoft.footballscores.service.myFetchRetroService;
import barqsoft.footballscores.service.myFetchService;
import barqsoft.footballscores.shared.AppSharedPreference;
import barqsoft.footballscores.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainScreenFragment.class.getName();
    private static final java.lang.String LEAGUE_ID = "LEAGUE_ID";
    private int league_id;
    public scoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    public static final int SCORES_LOADER_LEAGUE_ID = 1;
    private String[] fragmentdate = new String[1];
    private int last_selected_item = -1;
    ListViewSrollListener scrollinListener;
    ListView score_list;
    View rootView;
    AppSharedPreference appSharedPreference;
    private TextView emptyView;

    public MainScreenFragment() {
    }

    public void setScrollinListener(ListViewSrollListener scrollinListener) {
        this.scrollinListener = scrollinListener;
    }

    private void update_scores() {
        Intent service_start = new Intent(getActivity(), myFetchRetroService.class);
        getActivity().startService(service_start);
    }

    public void setFragmentDate(String date) {
        fragmentdate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        appSharedPreference = new AppSharedPreference(getActivity());
        appSharedPreference.getSharedPrefence().registerOnSharedPreferenceChangeListener(this);
        update_scores();

        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        score_list = (ListView) rootView.findViewById(R.id.scores_list);
        emptyView = (TextView) rootView.findViewById(R.id.empty);
        mAdapter = new scoresAdapter(getActivity(), null, 0, this);
        score_list.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.detail_match_id = MainActivity.selected_match_id;


        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int positsion, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });

        score_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            int firstVisibleItem;
            int lastVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

                if (scrollinListener != null) {
                    if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        scrollinListener.scrollingStopped();
                    } else {
                        scrollinListener.scrolling();
                    }
                }

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                firstVisibleItem = i;
                lastVisibleItem = i1 + i;
            }


        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, " loading ID:" + i);
        switch (i) {
            case SCORES_LOADER:
                return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                        null, null, fragmentdate, null);

            case SCORES_LOADER_LEAGUE_ID: {
                Log.d(TAG, " loading SCORES_LOADER_LEAGUE_ID");
                String league_id_and_date[] = new String[]{bundle.getString(LEAGUE_ID), fragmentdate[0]};
                return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithLeagueAndDate(),
                        null, null, league_id_and_date, null);
            }
        }
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, fragmentdate, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */


        if (cursor.getCount() == 0) {

            Log.d(TAG, "cursor empty");

            if (Utils.isNetworkAvailable(getActivity())) {

                emptyView.setText(R.string.no_data_available);
            } else {
                emptyView.setText(R.string.no_internet);
            }
            score_list.setEmptyView(emptyView);
        }


        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            i++;
            cursor.moveToNext();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(TAG, "s:" + s);

        if (sharedPreferences.contains(s)) {
            handleError();
        }

    }

    private void handleError() {
        switch (appSharedPreference.getError()) {
            case ApiError.BAD_REQUEST: {

                break;
            }
            case ApiError.NO_INTERNET: {
                emptyView.setText(R.string.no_internet);
                score_list.setEmptyView(emptyView);
                break;
            }
            case ApiError.EXCEEDED_REQUEST_LIMIT: {
                //show dialog
                break;
            }
            default:
                break;
        }

    }


    public interface ListViewSrollListener {
        public void scrolling();

        public void scrollingStopped();

    }

    @Bind(R.id.epl)
    FloatingActionButton fabEpl;

    @OnClick(R.id.epl)
    public void eplClicked() {
        Log.d(TAG, "epl clicked");
        filterLeague(fabEpl);
    }

    @Bind(R.id.primera)
    FloatingActionButton fabPrimera;

    @OnClick(R.id.primera)
    public void primeraClicked() {
        Log.d(TAG, "primeraClicked");
        filterLeague(fabPrimera);
    }

    @Bind(R.id.seriaa)
    FloatingActionButton fabSeriea;

    @OnClick(R.id.seriaa)
    public void serieaClicked() {
        Log.d(TAG, "seriea clicked");
        filterLeague(fabSeriea);
    }

    @Bind(R.id.bundesliga)
    FloatingActionButton fabBundesliga;

    @OnClick(R.id.bundesliga)
    public void bundesligaClicked() {
        Log.d(TAG, "bundesliga");
        filterLeague(fabBundesliga);
    }

    @Bind(R.id.champions_league)
    FloatingActionButton fabCL;

    @OnClick(R.id.champions_league)
    public void championsLeagueClicked(View v) {
        Log.d(TAG, "champions League");
        filterLeague(fabCL);
    }

    @Bind(R.id.consolidated)
    FloatingActionButton fabConsolidated;

    @OnClick(R.id.consolidated)
    public void consolidatedScoresClicked(View v) {
        Log.d(TAG, "consolidated scores");
        filterLeague(fabConsolidated);
    }


    public void filterLeague(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.epl: {

                bundle.putString(LEAGUE_ID, "398");

                break;
            }
            case R.id.primera: {
                bundle.putString(LEAGUE_ID, "399");

                break;
            }
            case R.id.seriaa: {
                bundle.putString(LEAGUE_ID, "401");

                break;
            }
            case R.id.bundesliga: {
                bundle.putString(LEAGUE_ID, "403");

                break;
            }
            case R.id.champions_league: {
                bundle.putString(LEAGUE_ID, "405");
                break;
            }
            default:{
                getActivity().setTitle(getString(R.string.app_name));
                getLoaderManager().restartLoader(SCORES_LOADER, bundle, this);
                return;
            }
        }
        getActivity().setTitle(getTitle(bundle.getString(LEAGUE_ID)) + " "+getString(R.string.scores));
        getLoaderManager().restartLoader(SCORES_LOADER_LEAGUE_ID, bundle, this);
        Log.d(TAG, "loader initiated");
    }

    public String getTitle(String id) {
        Realm realm = Realm.getInstance(getActivity());
        League league = realm.where(League.class).equalTo("id", id).findFirst();
        return league.getLeagueName();
    }
}
