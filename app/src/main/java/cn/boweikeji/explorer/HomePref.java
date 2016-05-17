package cn.boweikeji.explorer;

import org.androidannotations.annotations.sharedpreferences.DefaultRes;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by shizhongyong on 5/17/2016.
 */
@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface HomePref {

    @DefaultRes(R.string.default_homepage)
    String homepage();

}
