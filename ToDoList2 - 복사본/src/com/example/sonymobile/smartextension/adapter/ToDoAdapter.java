package com.example.sonymobile.smartextension.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

import com.example.sonymobile.smartextension.todolist.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

@SuppressLint("NewApi")
public class ToDoAdapter extends BaseAdapter
{
	public final static int VIEW = 0;
	public final static int MODIFY = 1;
	public final static int DELETE = 2;
	public final static String[] weeks = { "��", "��", "ȭ", "��", "��", "��", "��" };
	public final static String[] am_pm = { "����", "����" };
	private Context context; // �ʿ��� ��� �ʵ�
	private int layoutRes; // �� �ϳ��� ���� ���̾ƿ� ������ ������ �ִ� ���ҽ�(R.layout.todo)
	private ArrayList<ToDo> list; // ���� ����� �����͸� ������ �ִ� �迭
	private LayoutInflater inflater; //�� �ϳ��ϳ��� ���̾ƿ��� ������ ���̾ƿ� ������ ��ü(*)
	private DoneAdapter doneAdapter;
	private Button button_check;
	private Button button_list;
	private Button button_delete;
	private LinearLayout layout;
	private AlertDialog alertdialog;
	private SharedPreferences pref;
	private SharedPreferences.Editor prefEditor;
	private String str_todo;
	private String str_calendar;
	private String str_check;
	private StringTokenizer st_todo;
	private StringTokenizer st_calendar;
	private StringTokenizer st_check;
	private StringBuilder sb_todo;
	private StringBuilder sb_calendar;
	private StringBuilder sb_check;
	private boolean longClick;
	private TextView textView_view_todo;
	private TextView textView_view_time;
	private EditText editText_modify_todo;
	private CheckBox checkBox_modify;
	private Button button_modify_date;
	private Button button_modify_time;
	private Button positiveButton;
	private Calendar calendar;
	private boolean check;
	private int ampm;
	
	public ToDoAdapter(Context context, int layoutRes, SharedPreferences pref)
	{
		this.context = context;
		this.layoutRes = layoutRes;
		this.pref = pref;
		
		list = new ArrayList<ToDo>();
		str_todo = pref.getString("todo", null);
		str_calendar = pref.getString("calendar_todo", null);
		str_check = pref.getString("check_todo", null);
		
		if(str_todo!=null)
		{
			st_todo = new StringTokenizer(str_todo, ",,");
			st_calendar = new StringTokenizer(str_calendar, ",");
			st_check = new StringTokenizer(str_check, ",");
			while(st_todo.hasMoreTokens())
			{
				ToDo todo = new ToDo();
				todo.setTodo(st_todo.nextToken());
				todo.setCalendar(st_calendar.nextToken());
				todo.setCheck(Boolean.parseBoolean(st_check.nextToken()));
				list.add(todo);
			}
		}
		
		// ���̾ƿ� ������ ��ü ������(*)(todo.xml������ �о�鿩�� View��ü�� �������ִ� Ŭ����)
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount()
	{
		return list.size();
	}
	
	@Override
	public Object getItem(int position)
	{
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	// ����Ʈ���� ���� ������ �� ȣ��Ǵ� �޼ҵ�(��ũ���� �����̸� ���ο� �����͵��� �������� �Ǵµ� ������������ �� �޼ҵ尡 ȣ��ȴ�.
	// �� �޼ҵ� �ȿ��� �ϳ��� �� ������� ������ �ϰ� �������ش�.)
	// (��ȣ(position), �� �࿡ �ش��ϴ� ��(convertView), ����ListView(parent))
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// ���� ���� ����ִٸ� list.xml�� ���·� �������� ������ش�.
		if(convertView == null)
			// Ŀ���͸���¡�� �� ���̾ƿ��� �̿��ؼ� �並 �����.(*)
			convertView = inflater.inflate(layoutRes, parent, false);
		
		button_check = (Button)convertView.findViewById(R.id.button_check_todo);
		button_list = (Button)convertView.findViewById(R.id.button_list_todo);
		button_delete = (Button)convertView.findViewById(R.id.button_delete_todo);
		
		// ����Ʈ�� ����ִ� ����� �ؽ�Ʈ�� ���´�.
		button_list.setText(list.get(position).getTodo());
		button_list.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				if(!longClick)
					createAlert(VIEW, position);
				longClick = false;
			}
		});
		
		button_list.setOnLongClickListener(new OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				longClick = true;
				createAlert(MODIFY, position);
				return false;
			}
		});
		
		button_delete.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				createAlert(DELETE, position);
			}
		});
		
		button_check.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ToDo todo = list.get(position);
				doneAdapter.add(todo);
				list.remove(todo);
				updateData();
			}
		});
		
		//������ �並 �����Ѵ�.
		return convertView;
	}
	
	public void add(ToDo todo)
	{
		list.add(todo);
		updateData();
	}
	
	public void linkAdapter(DoneAdapter adapter)
	{
		doneAdapter = adapter;
	}
	
	public void createAlert(int id, final int position)
	{
		switch (id)
		{
		case VIEW:
			layout = (LinearLayout)View.inflate(context, R.layout.todolist_view, null);
			textView_view_todo = (TextView)layout.findViewById(R.id.textView_view_todo);
			textView_view_time = (TextView)layout.findViewById(R.id.textView_view_time);
			
			textView_view_todo.setText(list.get(position).getTodo());
			if(!list.get(position).getCheck())
			{
				st_calendar = new StringTokenizer(list.get(position).getCalendar(), ".");
				String cal = st_calendar.nextToken() + "-" + st_calendar.nextToken() + "-" +
						st_calendar.nextToken() + " (" + st_calendar.nextToken() + ") " +
						st_calendar.nextToken() + " " + st_calendar.nextToken() + ":" +
						st_calendar.nextToken();
				textView_view_time.setText(cal);
			}
			else
				textView_view_time.setText("�Ⱓ ����");
			
			new AlertDialog.Builder(context)
				.setView(layout)
				.setTitle("�� �� ����")
				.setPositiveButton("OK", null).show();
			break;
		case MODIFY:
			layout = (LinearLayout)View.inflate(context, R.layout.todolist_modify, null);
			editText_modify_todo = (EditText)layout.findViewById(R.id.editText_modify_todo);
			checkBox_modify = (CheckBox)layout.findViewById(R.id.checkBox_modify);
			button_modify_date = (Button)layout.findViewById(R.id.button_modify_date);
			button_modify_time = (Button)layout.findViewById(R.id.button_modify_time);
			
			check = list.get(position).getCheck();
			calendar = Calendar.getInstance();
			if(!check)
			{
				st_calendar = new StringTokenizer(list.get(position).getCalendar(), ".");
				calendar.set(Integer.parseInt(st_calendar.nextToken()),
						Integer.parseInt(st_calendar.nextToken())-1,
						Integer.parseInt(st_calendar.nextToken()));
				st_calendar.nextToken();
				if(st_calendar.nextToken().equals("����"))
					ampm = 0;
				else
					ampm = 1;
				calendar.set(Calendar.AM_PM, ampm);
				calendar.set(Calendar.HOUR, Integer.parseInt(st_calendar.nextToken()));
				calendar.set(Calendar.MINUTE, Integer.parseInt(st_calendar.nextToken()));
			}
			else
			{
				checkBox_modify.setChecked(true);
				button_modify_date.setEnabled(false);
				button_modify_time.setEnabled(false);
			}
			
			editText_modify_todo.setText(list.get(position).getTodo());
			editText_modify_todo.setSelection((int)editText_modify_todo.getText().length());
			button_modify_date.setText(calendar.get(Calendar.YEAR) + ". " +
					(calendar.get(Calendar.MONTH)+1) + ". " + calendar.get(Calendar.DATE) + ". ("
					+ weeks[calendar.get(Calendar.DAY_OF_WEEK)-1] + ")");
			button_modify_time.setText(am_pm[calendar.get(Calendar.AM_PM)] + " " +
					calendar.get(Calendar.HOUR) + ":" +
					((calendar.get(Calendar.MINUTE)<10) ? "0" : "") +
					calendar.get(Calendar.MINUTE));
			
			checkBox_modify.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					check = isChecked;
					if(check)
					{
						button_modify_date.setEnabled(false);
						button_modify_time.setEnabled(false);
					}
					else
					{
						button_modify_date.setEnabled(true);
						button_modify_time.setEnabled(true);
					}
				}
			});
			
			button_modify_date.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					OnDateSetListener callBack = new OnDateSetListener()
					{
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth)
						{
							calendar.set(year, monthOfYear, dayOfMonth);
							button_modify_date.setText(year + ". " + (monthOfYear+1) + ". " +
							dayOfMonth + ". (" + weeks[calendar.get(Calendar.DAY_OF_WEEK)-1] +
							")");
						}
					};
					new DatePickerDialog(context, callBack, calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)).show();
				}
			});
			
			button_modify_time.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					OnTimeSetListener callBack = new OnTimeSetListener()
					{
						@Override
						public void onTimeSet(TimePicker view, int hourOfDay, int minute)
						{
							int ampm = 0;
							if(hourOfDay >= 12)
								ampm = 1;
							calendar.set(Calendar.AM_PM, ampm);
							calendar.set(Calendar.HOUR, hourOfDay%12);
							calendar.set(Calendar.MINUTE, minute);
							button_modify_time.setText(am_pm[ampm] + " " +
									((hourOfDay==0) ? 12 : hourOfDay%12) + ":" +
									((minute<10) ? "0" : "") + minute);
						}
					};
					new TimePickerDialog(context, callBack, calendar.get(Calendar.HOUR_OF_DAY),
							calendar.get(Calendar.MINUTE), false).show();
				}
			});
			
			alertdialog = new AlertDialog.Builder(context)
			.setView(layout)
			.setTitle("�� �� ����")
			.setPositiveButton("OK", null)
			.setNegativeButton("CANCEL", null)
			.create();
			alertdialog.setOnShowListener(new DialogInterface.OnShowListener()
			{
				@Override
				public void onShow(DialogInterface dialog)
				{
					positiveButton = alertdialog.getButton(AlertDialog.BUTTON_POSITIVE);
					positiveButton.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View view)
						{
							String todo_text = editText_modify_todo.getText().toString();
							if(todo_text.equals(""))
							{
								new AlertDialog.Builder(context)
								.setTitle("�� ���� �Է��ϼ���.")
								.setNegativeButton("Ȯ��", null).show();
							}
							else
							{
								String cal = null;
								if(!check)
									cal = calendar.get(Calendar.YEAR) + "." +
										(calendar.get(Calendar.MONTH)+1) + "." +
										calendar.get(Calendar.DATE) + "." +
										weeks[calendar.get(Calendar.DAY_OF_WEEK)-1] + "." +
										am_pm[calendar.get(Calendar.AM_PM)] + "." +
										calendar.get(Calendar.HOUR) + "." +
										calendar.get(Calendar.MINUTE);
								ToDo todo = new ToDo();
								todo.setTodo(todo_text);
								todo.setCalendar(cal);
								todo.setCheck(check);
								list.set(position, todo);
								updateData();
								alertdialog.dismiss();
							}
						}
					});
				}
			});
			alertdialog.show();
			break;
		case DELETE:
			new AlertDialog.Builder(context)
			.setTitle("�� �� ����")
			.setMessage("���� �����Ͻðڽ��ϱ�?")
			.setPositiveButton("OK", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					list.remove(list.get(position));
					updateData();
				}
			})
			.setNegativeButton("CANCEL", null).show();
		default:
			break;
		}
	}
	
	public void updateData()
	{
		//����� �����͸� �˸���.
		notifyDataSetChanged();
		sb_todo = new StringBuilder();
		sb_calendar = new StringBuilder();
		sb_check = new StringBuilder();
		for(int position=0; position<getCount(); position++)
		{
			sb_todo.append(list.get(position).getTodo()).append(",,");
			sb_calendar.append(list.get(position).getCalendar()).append(",");
			sb_check.append(list.get(position).getCheck()).append(",");
		}
		
		prefEditor = pref.edit();
		prefEditor.putString("todo", sb_todo.toString());
		prefEditor.putString("calendar_todo", sb_calendar.toString());
		prefEditor.putString("check_todo", sb_check.toString());
		prefEditor.commit();
	}
}