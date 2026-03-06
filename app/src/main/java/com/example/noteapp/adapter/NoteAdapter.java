package com.example.noteapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noteapp.R;
import com.example.noteapp.db.NoteEntity;
import com.example.noteapp.ui.components.GlassContainerView;
import com.example.noteapp.utils.HapticFeedbackUtil;
import com.example.noteapp.utils.AnimationManager;
import com.example.noteapp.utils.EncryptionUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 笔记列表适配器 - 支持上下文菜单和共享功能
 * 已适配自动解密预览内容、标签显示及时间胶囊功能
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    
    private List<NoteEntity> notes = new ArrayList<>();
    private OnNoteClickListener listener;
    private Context context;
    private int lastAnimatedPosition = -1;
    private EncryptionUtil encryptionUtil;
    private int[] cardColors;
    
    private int[] getCardColors(Context ctx) {
        if (cardColors == null) {
            android.content.res.TypedArray ta;
            int[] attrs = {R.attr.themeCardMint, R.attr.themeCardCream, R.attr.themeCardBlue, R.attr.themeCardLavender};
            ta = ctx.obtainStyledAttributes(attrs);
            cardColors = new int[4];
            cardColors[0] = ta.getColor(0, ContextCompat.getColor(ctx, R.color.note_card_mint));
            cardColors[1] = ta.getColor(1, ContextCompat.getColor(ctx, R.color.note_card_cream));
            cardColors[2] = ta.getColor(2, ContextCompat.getColor(ctx, R.color.note_card_blue));
            cardColors[3] = ta.getColor(3, ContextCompat.getColor(ctx, R.color.note_card_lavender));
            ta.recycle();
        }
        return cardColors;
    }
    
    public interface OnNoteClickListener {
        void onNoteClick(NoteEntity note);
        void onNoteDelete(NoteEntity note);
        void onNoteShare(NoteEntity note);
        void onNoteShareImages(NoteEntity note);
        void onNoteCopy(NoteEntity note);
    }
    
    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }
    
    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        this.encryptionUtil = EncryptionUtil.getInstance(context);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        NoteEntity note = notes.get(position);
        holder.bind(note);
        animateCardEntry(holder.itemView, position);
    }
    
    @Override
    public int getItemCount() {
        return notes.size();
    }
    
    public void setNotes(List<NoteEntity> notes) {
        this.notes = notes;
        lastAnimatedPosition = -1;
        notifyDataSetChanged();
    }

    private void animateCardEntry(View itemView, int position) {
        if (position > lastAnimatedPosition) {
            itemView.setAlpha(0f);
            itemView.setTranslationY(38f);
            itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(position * 35L)
                    .setDuration(280)
                    .start();
            lastAnimatedPosition = position;
        }
    }
    
    class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private TextView titleView;
        private TextView contentView;
        private TextView dateView;
        private TextView categoryTagView;
        private ImageButton deleteButton;
        private FrameLayout cardArtLayer;
        
        // 时间胶囊相关UI
        private View timeCapsuleIndicator;
        private ImageView capsuleStatusIcon;
        private TextView capsuleCountdownText;
        private View contentBlurView;
        
        private NoteEntity currentNote;
        
        public NoteViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.tv_title);
            contentView = itemView.findViewById(R.id.tv_content);
            dateView = itemView.findViewById(R.id.tv_date);
            categoryTagView = itemView.findViewById(R.id.tv_category_tag);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            cardArtLayer = itemView.findViewById(R.id.card_art_layer);
            
            timeCapsuleIndicator = itemView.findViewById(R.id.layout_time_capsule_indicator);
            capsuleStatusIcon = itemView.findViewById(R.id.iv_capsule_status);
            capsuleCountdownText = itemView.findViewById(R.id.tv_capsule_countdown);
            contentBlurView = itemView.findViewById(R.id.view_content_blur);
            
            itemView.setOnLongClickListener(this);
        }
        
        @Override
        public boolean onLongClick(View v) {
            // 如果是已封存的时间胶囊，限制长按菜单（或提示不可操作）
            if (currentNote.isSealed()) {
                HapticFeedbackUtil.warning(v);
                return true; 
            }
            
            HapticFeedbackUtil.tap(v);
            
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle(currentNote.getTitle().isEmpty() ? "笔记" : currentNote.getTitle());
            
            CharSequence[] options = {"编辑", "分享文本", "分享图片", "复制", "删除"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: if (listener != null) listener.onNoteClick(currentNote); break;
                    case 1: if (listener != null) listener.onNoteShare(currentNote); break;
                    case 2: if (listener != null) listener.onNoteShareImages(currentNote); break;
                    case 3: if (listener != null) listener.onNoteCopy(currentNote); break;
                    case 4: if (listener != null) listener.onNoteDelete(currentNote); break;
                }
            });
            
            builder.show();
            return true;
        }
        
        public void bind(NoteEntity note) {
            this.currentNote = note;

            if (itemView instanceof GlassContainerView) {
                int adapterPosition = getBindingAdapterPosition();
                if (adapterPosition < 0) adapterPosition = 0;
                int[] colors = getCardColors(itemView.getContext());
                int color = colors[adapterPosition % colors.length];
                ((GlassContainerView) itemView).setGlassColor(color);
            }

            renderGenerativeArt(note);
            
            String title = note.getTitle();
            if (title == null || title.isEmpty()) {
                title = itemView.getContext().getString(R.string.no_title);
            }
            titleView.setText(title);
            
            // 显示分类和标签
            StringBuilder tagDisplay = new StringBuilder(note.getCategory());
            String tags = note.getTags();
            if (tags != null && !tags.isEmpty()) {
                tagDisplay.append(" | ").append(tags.replace(",", ", "));
            }
            categoryTagView.setText(tagDisplay.toString());
            categoryTagView.setVisibility(View.VISIBLE);
            
            // 处理时间胶囊逻辑
            if (note.isTimeCapsule()) {
                timeCapsuleIndicator.setVisibility(View.VISIBLE);
                boolean isSealed = note.isSealed();
                
                if (isSealed) {
                    capsuleStatusIcon.setImageResource(R.drawable.ic_lock);
                    timeCapsuleIndicator.setBackgroundResource(R.drawable.capsule_badge_bg);
                    long days = note.getRemainingDays();
                    if (days > 0) {
                        capsuleCountdownText.setText("剩余 " + days + " 天");
                    } else {
                        capsuleCountdownText.setText("即将开启");
                    }
                    contentBlurView.setVisibility(View.VISIBLE);
                    // 隐藏原内容文本，用遮罩展示
                    contentView.setText("");
                } else {
                    // 已开启 - 使用绿色徽章
                    capsuleStatusIcon.setImageResource(R.drawable.ic_lock_open);
                    capsuleCountdownText.setText("已开启");
                    timeCapsuleIndicator.setBackgroundResource(R.drawable.capsule_badge_opened_bg);
                    contentBlurView.setVisibility(View.GONE);
                }
            } else {
                timeCapsuleIndicator.setVisibility(View.GONE);
                contentBlurView.setVisibility(View.GONE);
            }
            
            // 自动解密预览内容
            String rawContent = note.getContent();
            String displayContent;
            
            if (note.isSealed()) {
                // 封存状态下不显示内容
                displayContent = "";
            } else {
                try {
                    displayContent = encryptionUtil.decryptWithMaster(rawContent);
                } catch (Exception e) {
                    displayContent = rawContent; 
                }
                
                if (displayContent != null && displayContent.length() > 80) {
                    displayContent = displayContent.substring(0, 80) + "...";
                }
            }
            contentView.setText(displayContent);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            dateView.setText(sdf.format(new Date(note.getTimestamp())));
            
            itemView.setOnClickListener(v -> {
                if (note.isSealed()) {
                    HapticFeedbackUtil.warning(v);
                    android.widget.Toast.makeText(context, "时间胶囊尚未到期，无法查看", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                HapticFeedbackUtil.success(v);
                AnimationManager.createScaleAnimation(v, 0.95f, 100);
                if (listener != null) listener.onNoteClick(note);
            });
            
            deleteButton.setOnClickListener(v -> {
                HapticFeedbackUtil.warning(v);
                AnimationManager.createScaleAnimation(v, 0.9f, 100);
                if (listener != null) listener.onNoteDelete(note);
            });
        }

        private void renderGenerativeArt(NoteEntity note) {
            if (cardArtLayer == null) return;

            cardArtLayer.removeAllViews();
            cardArtLayer.setEnabled(false);
            cardArtLayer.setClickable(false);
            cardArtLayer.setFocusable(false);

            cardArtLayer.post(() -> {
                int width = cardArtLayer.getWidth();
                int height = cardArtLayer.getHeight();
                if (width <= 0 || height <= 0) return;

                long seed = note.getId() * 31L + note.getTimestamp();
                String title = note.getTitle();
                if (title != null) seed += title.hashCode();
                Random random = new Random(seed);

                int[] iconRes = {R.drawable.ic_doodle_flower, R.drawable.ic_doodle_leaf, R.drawable.ic_doodle_star};
                int[] artColors = {
                        ContextCompat.getColor(itemView.getContext(), R.color.ios_green),
                        ContextCompat.getColor(itemView.getContext(), R.color.ios_blue),
                        ContextCompat.getColor(itemView.getContext(), R.color.ios_purple),
                        ContextCompat.getColor(itemView.getContext(), R.color.ios_orange),
                        ContextCompat.getColor(itemView.getContext(), R.color.ios_pink)
                };

                for (int index = 0; index < 8; index++) {
                    ImageView icon = new ImageView(itemView.getContext());
                    icon.setImageResource(iconRes[random.nextInt(iconRes.length)]);
                    icon.setColorFilter(artColors[random.nextInt(artColors.length)]);

                    int size = index < 2 ? dpToPx(24) : dpToPx(14);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
                    params.leftMargin = random.nextInt(Math.max(1, width - size));
                    params.topMargin = random.nextInt(Math.max(1, height - size));

                    icon.setAlpha(index < 2 ? 0.4f : 0.2f);
                    icon.setRotation(random.nextInt(360));
                    cardArtLayer.addView(icon, params);
                }
            });
        }

        private int dpToPx(int dp) {
            float density = itemView.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
