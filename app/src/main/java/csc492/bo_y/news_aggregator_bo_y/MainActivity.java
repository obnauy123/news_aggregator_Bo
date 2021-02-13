package csc492.bo_y.news_aggregator_bo_y;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ViewPager viewPager;
    private MyPageAdapter pageAdapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Menu opt_menu;
    private HashMap<String, Provider> map = new HashMap<>();
    private Set<Provider> providers;
    private ArrayList<Content> contents;
    private HashMap<Integer,String> langMap;
    private HashMap<Integer,String> counMap;
    private List<Fragment> fragments;
    private Set<String> cateList;
    private String language = "all";
    private String country = "all";
    private String cate = "all";
    private String apiKey = "190873be39f547c68901fb07ffba65b4";
    private String curContent;
    private ArrayList<String> pro;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cateList = new HashSet<>();
        langMap = new HashMap<>();
        counMap = new HashMap<>();
        viewPager = findViewById(R.id.viewPager);
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mDrawerList = findViewById(R.id.content_list);
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectItem(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,       /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        fragments = new ArrayList<>();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());

        viewPager.setAdapter(pageAdapter);

        new Thread(new loadNewsProvider(apiKey,this)).start();


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }



    }
    private void selectItem(int position) {
        viewPager.setBackground(null);
        curContent = pro.get(position);
        new Thread(new loadNewsContent(apiKey, this,map.get(curContent).getId())).start();
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void loadResult(Set<Provider> s){
            pro = new ArrayList<>();
            this.providers = s;
            map = new HashMap<>();
            for(Provider p : providers){
                if(cate.equals("all") || p.getCategory().equals(cate)){
                    if(language.equals("all") || p.getLanguage().equals(language)){
                        if(country.equals("all") || p.getCountry().equals(country)){
                            pro.add(p.getName());
                            map.put(p.getName(),p);
                            cateList.add(p.getCategory());

                        }
                    }
                }

            }
            setTitle("News Gateway (" + pro.size() +")");
            Collections.sort(pro);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_item, pro) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent){
                    View v = super.getView(position,convertView,parent);
                    TextView et = (TextView) v.findViewById(R.id.text_view);
                    Provider cur = map.get(et.getText().toString());
                    String category = cur.getCategory();

                    switch (category){
                        case "all":
                            et.setTextColor(Color.BLACK);
                            break;
                        case "business":
                            et.setTextColor(Color.YELLOW);
                            break;
                        case "entertainment":
                            et.setTextColor(Color.MAGENTA);
                            break;
                        case "general":
                            et.setTextColor(Color.GREEN);
                            break;
                        case "health":
                            et.setTextColor(Color.BLUE);
                            break;
                        case "science":
                            et.setTextColor(Color.CYAN);
                            break;
                        case "sports":
                            et.setTextColor(Color.RED);
                            break;
                        case "technology":
                            et.setTextColor(Color.GRAY);
                            break;

                    }
                    return v;
                }
            };
            mDrawerList.setAdapter(adapter);
            ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();



            if(pro.size() == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("No News Sources Available when Topics is " + cate +", Language is " + language +" and Country is "+country);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }

                );
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        manageMenu();
        return true;
    }



    public void loadContent(ArrayList<Content> s){
        contents = s;
        setTitle(curContent);

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);
        fragments.clear();

        for (int i = 0; i < s.size(); i++) {
            fragments.add(
                    ContentFragment.newInstance(s.get(i), i+1, s.size()));
        }

        pageAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0);

    }

    private void manageMenu(){
        invalidateOptionsMenu();
        MenuItem topic = opt_menu.getItem(0);
        Menu topic_menu = topic.getSubMenu();
        topic_menu.add(3,0,0,"all");
        int j = 1;
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(cateList);
        Collections.sort(temp);
        for(String cur : temp){
            topic_menu.add(3,j,j,cur);
            j++;
        }

        for(int i = 1; i<=cateList.size(); i++){
            MenuItem c = topic_menu.getItem(i);
            SpannableString s = new SpannableString(c.toString());
            switch (i){
                case 1:
                    s.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, s.length(), 0);
                    c.setTitle(s);

                    break;
                case 2:
                    s.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, s.length(), 0);
                    c.setTitle(s);

                    break;
                case 3:
                    s.setSpan(new ForegroundColorSpan(Color.GREEN), 0, s.length(), 0);
                    c.setTitle(s);

                    break;
                case 4:
                    s.setSpan(new ForegroundColorSpan(Color.BLUE), 0, s.length(), 0);
                    c.setTitle(s);

                    break;
                case 5:
                    s.setSpan(new ForegroundColorSpan(Color.CYAN), 0, s.length(), 0);
                    c.setTitle(s);

                    break;
                case 6:
                    s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
                    c.setTitle(s);

                    break;
                case 7:
                    s.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s.length(), 0);
                    c.setTitle(s);

                    break;

            }

        }


        MenuItem coun = opt_menu.getItem(2);
        if(coun.hasSubMenu()){
            Menu coun_menu = (Menu) coun.getSubMenu();
            try {
                InputStream is = getResources().openRawResource(R.raw.country_codes);
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line);
                    line = reader.readLine();
                }
                JSONObject counjson = new JSONObject(sb.toString());
                JSONArray coun_array = counjson.getJSONArray("countries");

                coun_menu.add(2,0,0,"all");
                counMap.put(0,"all");
                for(int i =1; i<=coun_array.length(); i++){
                    JSONObject js = coun_array.getJSONObject(i-1);
                    coun_menu.add(2,i,i,js.getString("name"));
                    counMap.put(i,js.getString("code").toLowerCase());
                }


                reader.close();
            }catch (Exception e){

            }
        }else{
            Log.d(TAG, "manageMenu:fail ");
        }

        MenuItem lang = opt_menu.getItem(1);
        if(lang.hasSubMenu()){
            Menu lang_menu = (Menu) lang.getSubMenu();
            try {
                InputStream is = getResources().openRawResource(R.raw.language_codes);
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line);
                    line = reader.readLine();
                }
                JSONObject langjson = new JSONObject(sb.toString());
                JSONArray lang_array = langjson.getJSONArray("languages");

                lang_menu.add(1,0,0,"all");
                langMap.put(0,"all");
                for(int i =1; i<=lang_array.length(); i++){
                    JSONObject js = lang_array.getJSONObject(i-1);
                    lang_menu.add(1,i,i,js.getString("name"));
                    langMap.put(i,js.getString("code").toLowerCase());
                }
                reader.close();
            }catch (Exception e){
                Log.d("Fail", "onCreateOptionsMenu: ");
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        opt_menu = menu;

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        switch (item.getGroupId()){
            case 3:
                this.cate = item.getTitle().toString();
                loadResult(providers);
                return true;
            case 1:
                this.language = langMap.get(item.getItemId());
                Log.d(TAG, language);
                loadResult(providers);
                return true;
            case 2:
                this.country = counMap.get(item.getItemId());
                loadResult(providers);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }








    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        MyPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }
}