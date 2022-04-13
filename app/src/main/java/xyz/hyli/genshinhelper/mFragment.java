package xyz.hyli.genshinhelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class mFragment extends Fragment {
    private static final String ARG_VIEW = "概览";
    private View view;
    private String mView;


    public mFragment() {

    }


    public static mFragment newInstance(String ViewID) {
        mFragment fragment = new mFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIEW, ViewID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mView = getArguments().getString(ARG_VIEW);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        switch (mView) {
            case "工具" :
                view = inflater.inflate(R.layout.fragment_tools, container, false);
                break;
            case "图鉴" :
                view = inflater.inflate(R.layout.fragment_map, container, false);
                break;
            case "设置" :
                view = inflater.inflate(R.layout.fragment_settings, container, false);
                break;
            default:
                view = inflater.inflate(R.layout.fragment_main, container, false);
        }
        return view;
    }
}