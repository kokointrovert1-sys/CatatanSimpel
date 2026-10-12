package id.shopsimple.catatan;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.*;
import android.widget.*;
import org.json.*;
import java.text.DateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int ink = Color.rgb(36, 48, 43), green = Color.rgb(53,104,89), paper = Color.rgb(246,247,242), muted = Color.rgb(115,126,119);
    private ArrayList<Note> notes = new ArrayList<>();
    private LinearLayout root, list;
    private EditText search;
    private TextView count;
    private long nextId = 1;
    static class Note { long id; String title, body; long updated; Note(long i,String t,String b,long u){id=i;title=t;body=b;updated=u;} }

    @Override public void onCreate(Bundle b) { super.onCreate(b); getWindow().setStatusBarColor(paper); getWindow().setNavigationBarColor(paper); load(); home(); }
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    GradientDrawable bg(int color, int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    void pad(View v,int a,int b,int c,int d){v.setPadding(dp(a),dp(b),dp(c),dp(d));}
    void home(){
        root=column();root.setBackgroundColor(paper);pad(root,22,22,22,12);setContentView(root);
        LinearLayout top=row(); TextView brand=text("CATATAN",12,green,true); brand.setLetterSpacing(.14f); top.addView(brand,new LinearLayout.LayoutParams(0,dp(30),1));
        TextView add=text("＋",30,Color.WHITE,true);add.setGravity(Gravity.CENTER);add.setBackground(bg(green,18));top.addView(add,lp(52,52));add.setOnClickListener(v->edit(null));root.addView(top);
        TextView title=text("Simpan ide.\nJernihkan pikiran.",29,ink,true);title.setLineSpacing(dp(3),1f);LinearLayout.LayoutParams tp=lp(-1,-2);tp.topMargin=dp(26);root.addView(title,tp);
        TextView sub=text("Catatan kecil untuk hal-hal penting.",14,muted,false);LinearLayout.LayoutParams sp=lp(-1,-2);sp.topMargin=dp(8);root.addView(sub,sp);
        search=new EditText(this);search.setSingleLine(true);search.setTextSize(15);search.setHint("⌕   Cari catatan...");search.setHintTextColor(muted);search.setTextColor(ink);search.setBackground(bg(Color.WHITE,14));pad(search,16,0,16,0);LinearLayout.LayoutParams qp=lp(-1,52);qp.topMargin=dp(24);root.addView(search,qp);
        count=text("CATATAN TERBARU",11,muted,true);count.setLetterSpacing(.1f);LinearLayout.LayoutParams cp=lp(-1,-2);cp.topMargin=dp(24);cp.bottomMargin=dp(12);root.addView(count,cp);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);list=column();scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){render(s.toString());}public void afterTextChanged(Editable e){}});
        render("");
    }
    void render(String query){list.removeAllViews();ArrayList<Note> filtered=new ArrayList<>();String q=query.toLowerCase(Locale.ROOT).trim();for(Note n:notes)if((n.title+" "+n.body).toLowerCase(Locale.ROOT).contains(q))filtered.add(n);
        count.setText(q.isEmpty()?"CATATAN TERBARU  ·  "+notes.size():"HASIL PENCARIAN  ·  "+filtered.size());
        if(filtered.isEmpty()){LinearLayout empty=column();empty.setGravity(Gravity.CENTER);pad(empty,10,48,10,48);TextView icon=text(q.isEmpty()?"✎":"⌕",36,green,true);icon.setGravity(Gravity.CENTER);empty.addView(icon,lp(-1,56));TextView msg=text(q.isEmpty()?"Belum ada catatan":"Catatan tidak ditemukan",17,ink,true);msg.setGravity(Gravity.CENTER);empty.addView(msg);TextView hint=text(q.isEmpty()?"Ketuk tombol + untuk menulis catatan pertamamu.":"Coba kata kunci yang berbeda.",13,muted,false);hint.setGravity(Gravity.CENTER);LinearLayout.LayoutParams hp=lp(-1,-2);hp.topMargin=dp(8);empty.addView(hint,hp);list.addView(empty);return;}
        for(Note n:filtered){LinearLayout card=column();card.setBackground(bg(Color.WHITE,16));pad(card,17,16,17,16);LinearLayout.LayoutParams cardp=lp(-1,-2);cardp.bottomMargin=dp(12);list.addView(card,cardp);TextView t=text(n.title.isEmpty()?"Tanpa judul":n.title,17,ink,true);card.addView(t);if(!n.body.trim().isEmpty()){TextView body=text(n.body.replace('\n',' '),14,muted,false);body.setMaxLines(3);LinearLayout.LayoutParams bp=lp(-1,-2);bp.topMargin=dp(8);card.addView(body,bp);}TextView date=text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT, new Locale("id","ID")).format(new Date(n.updated)),11,green,false);LinearLayout.LayoutParams datep=lp(-1,-2);datep.topMargin=dp(13);card.addView(date,datep);card.setOnClickListener(v->edit(n));card.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Hapus catatan?").setMessage(n.title.isEmpty()?"Catatan ini akan dihapus.":n.title).setNegativeButton("Batal",null).setPositiveButton("Hapus",(d,w)->{notes.remove(n);save();render(search.getText().toString());Toast.makeText(this,"Catatan dihapus",Toast.LENGTH_SHORT).show();}).show();return true;});}
    }
    void edit(Note note){final boolean fresh=note==null;final long id=fresh?nextId++:note.id;final EditText title=new EditText(this), body=new EditText(this);title.setSingleLine(true);title.setTextSize(20);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setHint("Judul catatan");title.setTextColor(ink);title.setHintTextColor(muted);title.setText(fresh?"":note.title);body.setTextSize(16);body.setGravity(Gravity.TOP);body.setMinLines(8);body.setHint("Mulai menulis...\n\nTulis apa pun yang ingin kamu ingat.");body.setTextColor(ink);body.setHintTextColor(muted);body.setText(fresh?"":note.body);LinearLayout form=column();pad(form,22,4,22,4);form.addView(title,lp(-1,58));View line=new View(this);line.setBackgroundColor(Color.rgb(230,233,226));form.addView(line,lp(-1,1));LinearLayout.LayoutParams bp=lp(-1,-2);bp.topMargin=dp(12);form.addView(body,bp);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(fresh?"Catatan baru":"Edit catatan").setView(form).setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();dialog.setOnShowListener(x->{dialog.getButton(-1).setTextColor(green);dialog.getButton(-2).setTextColor(muted);dialog.getButton(-1).setOnClickListener(v->{String t=title.getText().toString().trim(), b=body.getText().toString().trim();if(t.isEmpty()&&b.isEmpty()){Toast.makeText(this,"Catatan masih kosong",Toast.LENGTH_SHORT).show();return;}Note target=null;for(Note n:notes)if(n.id==id){target=n;break;}if(target==null){target=new Note(id,t,b,System.currentTimeMillis());notes.add(0,target);}target.title=t;target.body=b;target.updated=System.currentTimeMillis();save();dialog.dismiss();home();Toast.makeText(this,"Catatan tersimpan",Toast.LENGTH_SHORT).show();});});dialog.show();dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);}
    void load(){try{String raw=getSharedPreferences("notes",MODE_PRIVATE).getString("items","[]");JSONArray a=new JSONArray(raw);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long id=o.optLong("id",nextId++);notes.add(new Note(id,o.optString("title"),o.optString("body"),o.optLong("updated",System.currentTimeMillis())));nextId=Math.max(nextId,id+1);}Collections.sort(notes,(x,y)->Long.compare(y.updated,x.updated));}catch(Exception ignored){}}
    void save(){try{JSONArray a=new JSONArray();for(Note n:notes){JSONObject o=new JSONObject();o.put("id",n.id);o.put("title",n.title);o.put("body",n.body);o.put("updated",n.updated);a.put(o);}getSharedPreferences("notes",MODE_PRIVATE).edit().putString("items",a.toString()).apply();}catch(Exception ignored){}}
}
