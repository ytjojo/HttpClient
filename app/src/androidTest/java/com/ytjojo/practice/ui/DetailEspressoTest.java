//package com.ytjojo.flux.ui;
//
//import android.support.test.rule.ActivityTestRule;
//
//import com.ytjojo.flux.R;
//import com.ytjojo.ui.DetailActivity;
//
//import org.junit.Rule;
//import org.junit.Test;
//
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.assertion.ViewAssertions.matches;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static android.support.test.espresso.matcher.ViewMatchers.withText;
///**
// * Created by Administrator on 2016/11/1 0001.
// */
//public class DetailEspressoTest {
//
//    @Rule
//    public ActivityTestRule<DetailActivity> mActivityRule =
//            new ActivityTestRule<>(DetailActivity.class);
//
//    @Test
//    public void testActivityShouldHaveText() throws InterruptedException {
//        onView(withId(R.id.text)).check(matches(withText("Hello Espresso!")));
//    }
//}
