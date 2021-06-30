package com.zml.frescophotoviewdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zml.frescophotoviewdemo.R;

import com.zml.frescophotoview.transition.PhotoTransitionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/8 3:18 下午
 */
public class TransitionDemoActivity extends Activity {
    RecyclerView rv;
    private static final String[] SAMPLE_URIS = {
            "https://www.gstatic.com/webp/gallery/1.sm.jpg",
            "https://www.gstatic.com/webp/gallery/2.sm.jpg",
            "https://www.gstatic.com/webp/gallery/3.sm.jpg",
            "https://www.gstatic.com/webp/gallery/4.sm.jpg",
            "https://www.gstatic.com/webp/gallery/5.sm.jpg",
            "https://www.gstatic.com/webp/gallery/1.sm.jpg",
            "https://www.gstatic.com/webp/gallery/2.sm.jpg",
            "https://www.gstatic.com/webp/gallery/3.sm.jpg",
            "https://www.gstatic.com/webp/gallery/4.sm.jpg",
            "https://www.gstatic.com/webp/gallery/5.sm.jpg",
    };

    private static int[][] PHOTO_WIDTH_HEIGHT = new int[][]{
            {320,214},{320,235},{320,180},{320,241},{320,235},
            {320,214},{320,235},{320,180},{320,241},{320,235}
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition_demo);
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RecyclerView.Adapter<VH>() {
            @NonNull
            @NotNull
            @Override
            public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_drawee_view,parent,false));
            }

            @Override
            public void onBindViewHolder(@NonNull @NotNull VH holder, int position) {
                holder.draweeView.setImageURI(SAMPLE_URIS[position]);
                holder.draweeView.setOnClickListener(v -> {
                    Rect imgLocationRect = PhotoTransitionUtils.calculateInitPhotoRect(holder.draweeView,
                            PHOTO_WIDTH_HEIGHT[position][0],PHOTO_WIDTH_HEIGHT[position][1]);
                    Intent intent = new Intent(TransitionDemoActivity.this,TransitionDemoActivity2.class);
                    intent.putExtra("img_location",PhotoTransitionUtils.rectToIntArray(imgLocationRect));
                    intent.putExtra("url",SAMPLE_URIS[position]);
                    startActivity(intent);
                });
            }

            @Override
            public int getItemCount() {
                return SAMPLE_URIS.length;
            }
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        SimpleDraweeView draweeView;

        public VH(@NonNull @NotNull View itemView) {
            super(itemView);
            draweeView = (SimpleDraweeView) itemView;
        }

    }

}
