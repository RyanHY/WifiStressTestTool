package com.wifi.wifistressapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by kirt-server on 2016/1/21.
 */
public class ItemsFragment extends Fragment {
    private ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.items_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = (ListView) getActivity().findViewById(R.id.item_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                switch (position) {
                    case 0:
                        OnOffFragment mOnOffFragment = new OnOffFragment();
                        ft.replace(R.id.fragment_container, mOnOffFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    case 1:
                        SuspendResumeFragment mSuspendResumeFragment = new SuspendResumeFragment();
                        ft.replace(R.id.fragment_container, mSuspendResumeFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    case 2:
                        PingFragment mPingFragment = new PingFragment();
                        ft.replace(R.id.fragment_container, mPingFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    case 3:
                        HotSpotOnOffFragment mHotSpotOnOffFragment = new HotSpotOnOffFragment();
                        ft.replace(R.id.fragment_container, mHotSpotOnOffFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    default:
                        Toast.makeText(getContext(), "Not yet implement!!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }
}
