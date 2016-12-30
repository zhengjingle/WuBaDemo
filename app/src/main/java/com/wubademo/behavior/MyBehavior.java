package com.wubademo.behavior;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.wubademo.com.util.Matrix;
import com.wubademo.R;
import com.wubademo.customview.MyRecyclerView;

/**
 * Created by zhengjingle on 2016/12/29.
 */

public class MyBehavior extends CoordinatorLayout.Behavior<View>{
    
    Activity act;
    View dependency;
    FrameLayout mFrameLayout;
    LinearLayout ll_search;
    MyRecyclerView mRecyclerView;
    
    Scroller scroller;
    Handler handler;
    
    boolean isPlayingAnimation;
    boolean isRefreshing;
    boolean isStoppingRefresh;
    
    LinearLayout ll_weather;
    ImageView iv_icon;
    ImageView iv_img;
    TextView tv_tip;

    double[] def;
    
    public MyBehavior(Context context) {
        super();
        scroller=new Scroller(context);
        handler=new Handler();
    }
    
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if(dependency!=null && dependency.getId()== R.id.dependency){
            this.dependency=parent.findViewById(R.id.dependency);
            mFrameLayout= (FrameLayout) parent.findViewById(R.id.frameLayout);
            iv_img=(ImageView) parent.findViewById(R.id.iv_img);
            ll_search= (LinearLayout) parent.findViewById(R.id.ll_search);
            mRecyclerView=(MyRecyclerView)parent.findViewById(R.id.recyclerView);

            ll_weather=(LinearLayout) parent.findViewById(R.id.ll_weather);
            iv_icon=(ImageView) parent.findViewById(R.id.iv_icon);
            tv_tip=(TextView) parent.findViewById(R.id.tv_tip);

            act=(Activity)dependency.getContext();
            return true;
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if(def==null){
            def=getParams();
        }
        
        float dependencyTranslationY=dependency.getTranslationY();
        
        //weather
        float weatherProcess=dependencyTranslationY/getDependentExpanded();
        if(weatherProcess<0)weatherProcess=0;
        if(weatherProcess>1)weatherProcess=1;
        ll_weather.setAlpha(weatherProcess);
        
        //icon
        if(dependencyTranslationY>getDependentExpanded()){
            if(dependencyTranslationY>getDependentRefresh())dependencyTranslationY=getDependentRefresh();
            
            float iconProcess=(dependencyTranslationY-getDependentExpanded())/(getDependentRefresh()-getDependentExpanded());
            double leftMargin=0;
            double topMargin=0;
            
            if(isStoppingRefresh){
                float middleMaxLeftMargin=(getIconMarginMaxLeft()-iv_icon.getWidth())/2f;
                leftMargin=middleMaxLeftMargin+(getIconMarginMaxLeft()-middleMaxLeftMargin)*(1-iconProcess);
//                topMargin=(getIconMarginMaxTop()-getIconMarginMinTop())*(1-iconProcess)+getIconMarginMinTop();
                topMargin=calcValueY(leftMargin);
            }else{
                float middleMaxLeftMargin=(getIconMarginMaxLeft()-iv_icon.getWidth())/2f+2*iv_icon.getWidth();
                leftMargin=middleMaxLeftMargin*iconProcess-2*iv_icon.getWidth();
//                topMargin=(getIconMarginMaxTop()-getIconMarginMinTop())*(1-iconProcess)+getIconMarginMinTop();
                topMargin=calcValueY(leftMargin);
            }
            CoordinatorLayout.LayoutParams layoutParams= ((CoordinatorLayout.LayoutParams) iv_icon.getLayoutParams());
            layoutParams.leftMargin=(int) leftMargin;
            layoutParams.topMargin=(int) topMargin;
            iv_icon.setLayoutParams(layoutParams);

            iv_icon.setRotation(iconProcess*360);
        }
        
        //img
        dependencyTranslationY=dependency.getTranslationY();
        if(dependencyTranslationY>getDependentCollapsed()){
            if(dependencyTranslationY>getDependentExpanded())dependencyTranslationY=getDependentExpanded();

            float imgProcess=(dependencyTranslationY-getDependentCollapsed())/(getDependentExpanded()-getDependentCollapsed());
            iv_img.setAlpha(imgProcess);
        }
        dependencyTranslationY=dependency.getTranslationY();
        if(dependencyTranslationY>getDependentExpanded()){
            if(dependencyTranslationY>getDependentRefresh())dependencyTranslationY=getDependentRefresh();

            float imgProcess=(dependencyTranslationY-getDependentExpanded())/(getDependentRefresh()-getDependentExpanded());
            iv_img.setScaleX(imgProcess*0.5f+1);
            iv_img.setScaleY(imgProcess*0.5f+1);
            iv_img.setPivotX(iv_img.getWidth()/2);
            iv_img.setPivotY(iv_img.getHeight());
        }
        
        //ll_search
        dependencyTranslationY=dependency.getTranslationY();
        if(dependencyTranslationY>getDependentCollapsed()){
            if(dependencyTranslationY>getDependentExpanded())dependencyTranslationY=getDependentExpanded();

            float searchProcess=(dependencyTranslationY-getDependentCollapsed())/(getDependentExpanded()-getDependentCollapsed());
            
            CoordinatorLayout.LayoutParams layoutParams= ((CoordinatorLayout.LayoutParams) ll_search.getLayoutParams());
            float rightMargin=(getSearchRightMarginExpanded()-getSearchRightMarginCollapsed())*searchProcess+getSearchRightMarginCollapsed();
            layoutParams.rightMargin=(int)rightMargin;
            ll_search.setLayoutParams(layoutParams);
            
            searchProcess=(1-0.3f)*searchProcess+0.3f;
            ll_search.setAlpha(searchProcess);
        }
        
        //tv_tip
        dependencyTranslationY=dependency.getTranslationY();
        float minTranslationY=(getDependentExpanded()+getDependentRefresh())/2f;
        if(dependencyTranslationY>minTranslationY && !isRefreshing){
            if(dependencyTranslationY>getDependentRefresh())dependencyTranslationY=getDependentRefresh();

            float tipProcess=(dependencyTranslationY-minTranslationY)/(getDependentRefresh()-minTranslationY);
            tv_tip.setAlpha(tipProcess);
        }else{
            tv_tip.setAlpha(0);
        }
        
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes== ViewCompat.SCROLL_AXIS_VERTICAL;
    }
    
    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        if(isRefreshing){
            
        }else {
            scroller.abortAnimation();
            isPlayingAnimation = false;
        }
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        if(isPlayingAnimation || isRefreshing){
            consumed[1]=dy;
            return;
        }
        
        if(dy>0){
            float newTranslationY = dependency.getTranslationY()-dy;
            float minY=getDependentCollapsed();
            if(newTranslationY>minY){
                consumed[1]=dy;
                move(newTranslationY);
            }
            
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if(dyUnconsumed<0){
            float newTranslationY = dependency.getTranslationY()-dyUnconsumed;
            float maxY=getDependentMaxRefresh();
            
            if(newTranslationY<maxY){
                move(newTranslationY);
            }
        }
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        if (!isPlayingAnimation && !isRefreshing) {
            return actionUp(velocityY);
        }
        return false;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
        if (!isPlayingAnimation && !isRefreshing) {
            actionUp(1000);
        }
    }
    
    private boolean actionUp(float velocity){
        float dependencyTranslationY=dependency.getTranslationY();
        
        //在折叠或展开的位置不发生动画滚动
        if(dependencyTranslationY== getDependentCollapsed() || dependencyTranslationY==getDependentExpanded()){
            return false;
        }

        float newTranslationY=0;
        
        //分情况判断怎么动画滚动
        float middleOfCollapsedExpanded=(getDependentCollapsed()+getDependentExpanded())/2;
        if(dependencyTranslationY<middleOfCollapsedExpanded){//折叠
            newTranslationY=getDependentCollapsed();
        }else if(dependencyTranslationY<getDependentExpanded()){//展开
            newTranslationY=getDependentExpanded();
        }else if(dependencyTranslationY<getDependentRefresh()){//展开
            newTranslationY=getDependentExpanded();
        }else if(dependencyTranslationY<getDependentMaxRefresh()){//刷新
            isRefreshing=true;
            mRecyclerView.setCanScroll(false);
            runRotateIcon();
            newTranslationY=getDependentRefresh();
        }else{
            return false;
        }
        
        if(velocity<1000)velocity=1000;
        animationScroll((int) dependencyTranslationY,(int) (newTranslationY - dependencyTranslationY),(int) (1000000 / Math.abs(velocity)));
        
        return true;
    }
    
    private void animationScroll(int startY,int dy,int duration){
        scroller.startScroll(0, startY, 0, dy, duration);
        handler.post(runnable);
        isPlayingAnimation=true;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (scroller.computeScrollOffset()) {
                move(scroller.getCurrY());
                handler.post(this);
            } else {
                if(isRefreshing && refreshListener!=null){
                    refreshListener.onRefresh();
                }else{
                    isPlayingAnimation = false;
                }
                
            }
        }
    };

    private void animationStopScroll(int startY,int dy,int duration){
        scroller.startScroll(0, startY, 0, dy, duration);
        handler.post(runnableStop);
        isPlayingAnimation=true;
    }

    private Runnable runnableStop = new Runnable() {
        @Override
        public void run() {

            if (scroller.computeScrollOffset()) {
                move(scroller.getCurrY());
                handler.post(this);
            } else {
                isPlayingAnimation = false;
                isRefreshing = false;
                isStoppingRefresh =false;
                mRecyclerView.setCanScroll(true);
            }
        }
    };

    private void move(float y){
        dependency.setTranslationY(y);
        mFrameLayout.setTranslationY(y);
        ll_search.setTranslationY(y);
        mRecyclerView.setTranslationY(y);
        iv_img.setTranslationY(y);

        //mRecyclerView高度
        CoordinatorLayout.LayoutParams layoutParams= ((CoordinatorLayout.LayoutParams) mRecyclerView.getLayoutParams());
        layoutParams.height= (int) (((CoordinatorLayout) mRecyclerView.getParent()).getHeight()-mRecyclerView.getY());
        mRecyclerView.setLayoutParams(layoutParams);
    }

    private float getDependentCollapsed() {
        return dependency.getResources().getDimension(R.dimen.dependency_collapsed);
    }

    private float getDependentExpanded() {
        return dependency.getResources().getDimension(R.dimen.dependency_expanded);
    }
    
    private float getDependentRefresh() {
        return dependency.getResources().getDimension(R.dimen.dependency_refresh);
    }
    
    private float getDependentMaxRefresh() {
        return dependency.getResources().getDimension(R.dimen.dependency_max_refresh);
    }

    private float getIconMarginMinLeft() {
        return dependency.getResources().getDimension(R.dimen.icon_minleft_margin);
    }

    private float getIconMarginMaxLeft() {
        return ((ViewGroup)dependency.getParent()).getWidth();
    }

    private float getIconMarginMinTop() {
        return dependency.getResources().getDimension(R.dimen.icon_mintop_margin);
    }

    private float getIconMarginMaxTop() {
        return dependency.getResources().getDimension(R.dimen.icon_maxtop_margin);
    }

    private float getSearchRightMarginCollapsed() {
        return dependency.getResources().getDimension(R.dimen.search_collapsed_right_margin);
    }

    private float getSearchRightMarginExpanded() {
        return dependency.getResources().getDimension(R.dimen.search_expanded_right_margin);
    }
    
    RefreshListener refreshListener;
    public interface RefreshListener{
        public void onRefresh();
    }
    
    public void setRefreshListener(RefreshListener refreshListener){
        this.refreshListener=refreshListener;
    }
    
    public void stopRefresh(){
        stopRotateIcon();
        float dependencyTranslationY=dependency.getTranslationY();
        float newTranslationY=getDependentExpanded();
        animationStopScroll((int) dependencyTranslationY,(int) (newTranslationY - dependencyTranslationY),1000000 / Math.abs(1000));
        isStoppingRefresh=true;
    }
    
    Thread mThreadIcon;
    boolean runRotateIcon;
    private void runRotateIcon(){
        if (mThreadIcon == null) {
            mThreadIcon=new Thread(){
                float degree=iv_icon.getRotation();
                public void run(){
                    while(runRotateIcon){
                        SystemClock.sleep(50);
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_icon.setRotation(degree);
                                degree+=45;
                            }
                        });
                    }
                }
            };
        }
        runRotateIcon=true;
        mThreadIcon.start();
    }
    
    private void stopRotateIcon(){
        runRotateIcon=false;
        mThreadIcon=null;
    }
    
    private double[] getParams(){
        double[] p1={0,getIconMarginMaxTop()};
        double[] p2={(getIconMarginMaxLeft()-iv_icon.getWidth())/2f,getIconMarginMinTop()};
        double[] p3={getIconMarginMaxLeft(),getIconMarginMaxTop()};

        double[][] matrix = {{p1[0],p1[1],1,-(Math.pow(p1[0],2)+Math.pow(p1[1],2))},
                {p2[0],p2[1],1,-(Math.pow(p2[0],2)+Math.pow(p2[1],2))},
                {p3[0],p3[1],1,-(Math.pow(p3[0],2)+Math.pow(p3[1],2))}};
        Matrix.simple(3, matrix);
        return Matrix.getResult(3, matrix);
    }
    
    private double calcValueY(double x){
        double a=1;
        double b=def[1];
        double c=Math.pow(x,2)+def[0]*x+def[2];
        
        double y=-b-Math.sqrt(Math.pow(b,2)-4*a*c);
        y/=2*a;
        return y;
    }
}
