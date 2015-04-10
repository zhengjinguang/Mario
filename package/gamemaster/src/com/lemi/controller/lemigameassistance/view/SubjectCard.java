package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.activity.SubjectDetailActivity;
import com.lemi.controller.lemigameassistance.model.SubjectModel;
import com.lemi.mario.base.utils.ViewUtils;

/**
 * ScaleAsyncImageView to show subject icon.
 *
 * @author zhengjinguang@letv.com (shining)
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectCard extends LinearLayout {

  private SubjectModel subjectModel;
  private InnerScaleAsyncImageView innerScaleAsyncImageView;

  public SubjectCard(Context context) {
    super(context);
  }

  public SubjectCard(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    initView();
  }

  private void initView() {
    innerScaleAsyncImageView = (InnerScaleAsyncImageView) findViewById(R.id.subject_image);
    innerScaleAsyncImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (subjectModel != null) {
          SubjectDetailActivity.launch(getContext(), subjectModel.getSid(), subjectModel.getName());
        }
      }
    });
  }

  public void setData(SubjectModel subjectModel) {
    this.subjectModel = subjectModel;
    innerScaleAsyncImageView.loadNetworkImage(subjectModel.getIconUrl(), R.drawable.subject_card_default);
  }

  public static SubjectCard newInstance(ViewGroup parent) {
    return (SubjectCard) ViewUtils.newInstance(parent, R.layout.subject_card);
  }
}
