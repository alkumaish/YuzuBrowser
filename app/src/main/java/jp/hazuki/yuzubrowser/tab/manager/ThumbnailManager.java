/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.tab.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Region;

import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.image.ImageCache;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

class ThumbnailManager {
    private final int height;
    private final int width;

    private ImageCache cache;

    ThumbnailManager(Context context) {
        float density = DisplayUtils.getDensity(context);
        height = (int) (density * 82 + 0.5f);
        width = (int) (density * 104 + 0.5f);
        cache = new ImageCache(0x200000);
    }

    void takeThumbnailIfNeeded(MainTabData data) {
        if (data != null && data.isFinished() && !data.isShotThumbnail()) {
            createWithCache(data);
        }
    }

    void removeThumbnailCache(String url) {
        cache.remove(url);
    }

    private void createWithCache(MainTabData data) {
        Bitmap bitmap = cache.getBitmap(data.getUrl());
        if (bitmap != null) {
            data.shotThumbnail(bitmap);
        } else {
            create(data);
        }
    }

    private void create(final MainTabData data) {
        Bitmap bitmap = createThumbnailImage(data.mWebView);
        if (bitmap != null) {
            cache.putBitmap(data.getUrl(), bitmap);
            data.shotThumbnail(bitmap);
        }
    }

    void forceTakeThumbnail(MainTabData data) {
        createWithCache(data);
    }

    public void destroy() {
        cache.dispose();
    }

    private Bitmap createThumbnailImage(CustomWebView webView) {
        int x = webView.getWebView().getMeasuredWidth();
        int y = (int) ((float) x / width * height + 0.5f);
        if (x <= 0 || y <= 0) return null;
        float scale = (float) width / x;
        int scroll = (int) (webView.getWebView().getScrollY() * scale + 0.5f);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(bitmap);
        localCanvas.translate(0, -scroll);
        localCanvas.clipRect(0, scroll, width, height + scroll, Region.Op.REPLACE);
        localCanvas.scale(scale, scale, 0.0f, 0.0f);

        webView.getWebView().draw(localCanvas);
        return bitmap;
    }
}
