package com.home.siiu.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.home.siiu.fragment.ContactFragment;
import com.home.siiu.fragment.HomeFragment;
import com.home.siiu.fragment.MessageFragment;
import com.home.siiu.fragment.ProfileFragment;

/**
 * Created by Tim Kern on 11/19/2017.
 */

public class PagerAdapter extends FragmentPagerAdapter {

    private int tabCount;

    public ContactFragment contactFragment;
    public HomeFragment homeFragment;
    public MessageFragment messageFragment;
    public ProfileFragment profileFragment;

    public PagerAdapter(FragmentManager fm, int numberOfTabs) {

        super(fm);
        this.tabCount = numberOfTabs;
        contactFragment = new ContactFragment();
        homeFragment = new HomeFragment();
        messageFragment = new MessageFragment();
        profileFragment = new ProfileFragment();
    }

    public Fragment getItem(int arg0) {

        switch(arg0) {
            case 0:
                return contactFragment;
            case 1:
                return homeFragment;
            case 2:
                return messageFragment;
            case 3:
                return profileFragment;
            default:
                break;
        }
        return null;
    }

    public int getCount() {

        return tabCount;
    }

    public ContactFragment getContactFragment() { return contactFragment; }

    public HomeFragment getHomeFragment() { return homeFragment; }

    public MessageFragment getMessageFragment() { return messageFragment; }

    public ProfileFragment getProfileFragment() { return profileFragment; }
}
