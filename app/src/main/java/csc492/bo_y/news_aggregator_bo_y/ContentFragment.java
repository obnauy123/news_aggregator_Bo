package csc492.bo_y.news_aggregator_bo_y;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class ContentFragment extends Fragment {


    public ContentFragment() {
        // Required empty public constructor
    }


    public static ContentFragment newInstance(Content content, int index, int max) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle(1);
        args.putSerializable("Content",content);
        args.putSerializable("Index",index);
        args.putSerializable("Count",max);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment_layout =  inflater.inflate(R.layout.fragment_content, container, false);
        Bundle args = getArguments();
        if(args!=null){
            final Content cur = (Content) args.getSerializable("Content");
            if (cur== null) {
                return null;
            }
            int index = args.getInt("Index");
            int total = args.getInt("Count");
            TextView title = fragment_layout.findViewById(R.id.headline);
            title.setOnClickListener(v -> clickImage(cur.getUrl()));
            title.setText(cur.getTitle());
            TextView date = fragment_layout.findViewById(R.id.content_date);
            String cur_date = cur.getPublishedAt().split("T")[0] +" "+ cur.getPublishedAt().split("T")[1].substring(0,5);
            date.setText(cur_date);
            TextView author = fragment_layout.findViewById(R.id.Content_author);
            String cur_author = cur.getAuthor();
            author.setText(cur_author.equals("null") ? "" : cur_author);
            TextView content = fragment_layout.findViewById(R.id.content_content);
            content.setOnClickListener(v -> clickImage(cur.getUrl()));
            String cur_content = cur.getDescription();
            content.setMovementMethod(new ScrollingMovementMethod());
            content.setText(cur_content.equals("null") ? "" : cur_content);
            TextView page = fragment_layout.findViewById(R.id.content_page);
            page.setText(String.format(Locale.US, "%d of %d", index, total));
            ImageView imageView = fragment_layout.findViewById(R.id.content_image);
            imageView.setOnClickListener(v -> clickImage(cur.getUrl()));
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            Picasso.get().load(cur.getUrlToImage())
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();

                        }

                        @Override
                        public void onError(Exception e) {
                            imageView.setImageResource(R.drawable.noimage);
                        }
                    });
            return fragment_layout;
        } else {
            return null;
        }

    }
    private void clickImage(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

}