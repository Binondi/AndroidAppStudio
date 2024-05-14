/*
 * This file is part of Android AppStudio [https://github.com/TS-Code-Editor/AndroidAppStudio].
 *
 * License Agreement
 * This software is licensed under the terms and conditions outlined below. By accessing, copying, modifying, or using this software in any way, you agree to abide by these terms.
 *
 * 1. **  Copy and Modification Restrictions  **
 *    - You are not permitted to copy or modify the source code of this software without the permission of the owner, which may be granted publicly on GitHub Discussions or on Discord.
 *    - If permission is granted by the owner, you may copy the software under the terms specified in this license agreement.
 *    - You are not allowed to permit others to copy the source code that you were allowed to copy by the owner.
 *    - Modified or copied code must not be further copied.
 * 2. **  Contributor Attribution  **
 *    - You must attribute the contributors by creating a visible list within the application, showing who originally wrote the source code.
 *    - If you copy or modify this software under owner permission, you must provide links to the profiles of all contributors who contributed to this software.
 * 3. **  Modification Documentation  **
 *    - All modifications made to the software must be documented and listed.
 *    - the owner may incorporate the modifications made by you to enhance this software.
 * 4. **  Consistent Licensing  **
 *    - All copied or modified files must contain the same license text at the top of the files.
 * 5. **  Permission Reversal  **
 *    - If you are granted permission by the owner to copy this software, it can be revoked by the owner at any time. You will be notified at least one week in advance of any such reversal.
 *    - In case of Permission Reversal, if you fail to acknowledge the notification sent by us, it will not be our responsibility.
 * 6. **  License Updates  **
 *    - The license may be updated at any time. Users are required to accept and comply with any changes to the license.
 *    - In such circumstances, you will be given 7 days to ensure that your software complies with the updated license.
 *    - We will not notify you about license changes; you need to monitor the GitHub repository yourself (You can enable notifications or watch the repository to stay informed about such changes).
 * By using this software, you acknowledge and agree to the terms and conditions outlined in this license agreement. If you do not agree with these terms, you are not permitted to use, copy, modify, or distribute this software.
 *
 * Copyright © 2024 Dev Kumar
 */

package com.tscodeeditor.android.appstudio.activities.resourcemanager;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tscodeeditor.android.appstudio.R;
import com.tscodeeditor.android.appstudio.activities.BaseActivity;
import com.tscodeeditor.android.appstudio.adapters.resourcemanager.LayoutManagerAdapter;
import com.tscodeeditor.android.appstudio.databinding.ActivityLayoutManagerBinding;
import com.tscodeeditor.android.appstudio.utils.serialization.DeserializerUtils;
import com.tscodeeditor.android.appstudio.vieweditor.models.LayoutModel;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class LayoutManagerActivity extends BaseActivity {
  // SECTION Constants
  public static final int LAYOUT_SECTION = 0;
  public static final int INFO_SECTION = 1;
  public static final int LOADING_SECTION = 2;

  private ActivityLayoutManagerBinding binding;

  /*
   * Contains the location of project directory.
   * For example: /../../Project/100
   */
  private File projectRootDirectory;

  /*
   * Contains the location of project directory.
   * For example: /../../Project/100/../res/files/layout
   */
  private File layoutDirectory;
  private File outputPath;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    binding = ActivityLayoutManagerBinding.inflate(getLayoutInflater());

    setContentView(binding.getRoot());

    binding.toolbar.setTitle(R.string.app_name);
    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    projectRootDirectory = new File(getIntent().getStringExtra("projectRootDirectory"));
    layoutDirectory = new File(getIntent().getStringExtra("layoutDirectory"));
    outputPath = new File(getIntent().getStringExtra("outputPath"));
    switchSection(LOADING_SECTION);
    loadLayouts();
  }

  private void loadLayouts() {
    Executors.newSingleThreadExecutor()
        .execute(
            () -> {
              ArrayList<LayoutModel> layouts = loadLayoutModelsList();
              runOnUiThread(
                  () -> {
                    if (layouts.size() == 0) {
                      setInfo(getString(R.string.no_layouts_yet));
                    } else {
                      LayoutManagerAdapter layoutsAdapter =
                          new LayoutManagerAdapter(LayoutManagerActivity.this, layouts);
                      binding.layoutList.setAdapter(layoutsAdapter);
                      binding.layoutList.setLayoutManager(
                          new LinearLayoutManager(LayoutManagerActivity.this));
                      switchSection(LAYOUT_SECTION);
                    }
                  });
            });
  }

  public ArrayList<LayoutModel> loadLayoutModelsList() {
    ArrayList<LayoutModel> list = new ArrayList<LayoutModel>();

    if (layoutDirectory.exists()) {
      File[] layoutsFilePath = layoutDirectory.listFiles();
      for (int layouts = 0; layouts < layoutsFilePath.length; ++layouts) {
        LayoutModel layout =
            DeserializerUtils.deserialize(layoutsFilePath[layouts], LayoutModel.class);
        if (layout != null) {
          list.add(layout);
        }
      }
    }

    return list;
  }

  public void switchSection(int section) {
    binding.resourceView.setVisibility(section == LAYOUT_SECTION ? View.VISIBLE : View.GONE);
    binding.infoSection.setVisibility(section == INFO_SECTION ? View.VISIBLE : View.GONE);
    binding.loading.setVisibility(section == LOADING_SECTION ? View.VISIBLE : View.GONE);
  }

  public void setInfo(String error) {
    switchSection(INFO_SECTION);
    binding.infoText.setText(error);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }
}
