package ppt.reshi.besttodo.view.tags;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ppt.reshi.besttodo.R;
import ppt.reshi.besttodo.model.Tag;

/**
 * Created by Marcin Regulski on 27.04.2017.
 */

class TagsAdapter extends ArrayAdapter<Tag> {
    public TagsAdapter(Context context, List<Tag> tags) {
        super(context, android.R.layout.simple_list_item_1, tags);
    }

    public void replaceDataWith(List<Tag> newItems) {
        clear();
        if (newItems != null) {
            addAll(newItems);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.tag_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.tag_title);
            holder.dot = (ImageView) convertView.findViewById(R.id.tag_dot);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Tag tag = getItem(position);
        holder.title.setText(tag.getTitle());


        ((GradientDrawable) holder.dot.getDrawable().mutate()).setColor(parseHexColor(tag.getColor()));
        return convertView;
    }

    @ColorInt
    private int parseHexColor(String hexString) {
        Integer r = Integer.parseInt(hexString.substring(1,3), 16);
        Integer g = Integer.parseInt(hexString.substring(3,5), 16);
        Integer b = Integer.parseInt(hexString.substring(5,7), 16);
        return Color.argb(255, r,g,b);
    }
    private class ViewHolder {
        public ImageView dot;
        public TextView title;
    }
}
