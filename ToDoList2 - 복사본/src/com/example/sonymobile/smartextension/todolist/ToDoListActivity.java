/*

Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.sonymobile.smartextension.todolist;

import java.util.Calendar;

import com.example.sonymobile.smartextension.adapter.DoneAdapter;
import com.example.sonymobile.smartextension.adapter.ToDo;
import com.example.sonymobile.smartextension.adapter.ToDoAdapter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ToDoListActivity extends Activity
{
	public final static String[] weeks = { "��", "��", "ȭ", "��", "��", "��", "��" };
	public final static String[] am_pm = { "����", "����" };
	Activity activity = this;
	private ListView listView_todo;
	private ListView listView_done;
	private LinearLayout layout;
	private AlertDialog alertdialog;
	private SharedPreferences pref;
	private ToDoAdapter todoAdapter;
	private DoneAdapter doneAdapter;
	private EditText editText_add_todo;
	private CheckBox checkBox_add;
	private Button button_add_date;
	private Button button_add_time;
	private Button positiveButton;
	private boolean check;
	private Calendar calendar = Calendar.getInstance();
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todolist_main);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		listView_todo = (ListView)findViewById(R.id.listView_todo);
		listView_done = (ListView)findViewById(R.id.listView_done);
		
		pref = (SharedPreferences)getSharedPreferences("todo", MODE_PRIVATE);
		pref = (SharedPreferences)getSharedPreferences("calendar_todo", MODE_PRIVATE);
		pref = (SharedPreferences)getSharedPreferences("check_todo", MODE_PRIVATE);
		pref = (SharedPreferences)getSharedPreferences("done", MODE_PRIVATE);
		pref = (SharedPreferences)getSharedPreferences("calendar_done", MODE_PRIVATE);
		pref = (SharedPreferences)getSharedPreferences("check_done", MODE_PRIVATE);
		
		todoAdapter = new ToDoAdapter(this, R.layout.list_todo, pref);
		listView_todo.setAdapter(todoAdapter);
		doneAdapter = new DoneAdapter(this, R.layout.list_done, pref);
		listView_done.setAdapter(doneAdapter);
		todoAdapter.linkAdapter(doneAdapter);
		doneAdapter.linkAdapter(todoAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.to_do_list, menu);

		return true;
	}
	
	@SuppressLint("NewApi")
	public void AddonClick(View v)
	{
		layout = (LinearLayout)View.inflate(this, R.layout.todolist_add, null);
		editText_add_todo = (EditText)layout.findViewById(R.id.editText_add_todo);
		checkBox_add = (CheckBox)layout.findViewById(R.id.checkBox_add);
		button_add_date = (Button)layout.findViewById(R.id.button_add_date);
		button_add_time = (Button)layout.findViewById(R.id.button_add_time);
		check = false;
		
		calendar = Calendar.getInstance();
		button_add_date.setText(calendar.get(Calendar.YEAR) + ". " +
				(calendar.get(Calendar.MONTH)+1) + ". " + calendar.get(Calendar.DATE) + ". (" +
				weeks[calendar.get(Calendar.DAY_OF_WEEK)-1] + ")");
		button_add_time.setText(am_pm[calendar.get(Calendar.AM_PM)] + " " +
				calendar.get(Calendar.HOUR) + ":" +
				((calendar.get(Calendar.MINUTE)<10) ? "0" : "") + calendar.get(Calendar.MINUTE));
		
		checkBox_add.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				check = isChecked;
				if(check)
				{
					button_add_date.setEnabled(false);
					button_add_time.setEnabled(false);
				}
				else
				{
					button_add_date.setEnabled(true);
					button_add_time.setEnabled(true);
				}
			}
		});
		button_add_date.setOnClickListener(new OnClickListener()
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
						button_add_date.setText(year + ". " + (monthOfYear+1) + ". " + dayOfMonth
								+ ". (" + weeks[calendar.get(Calendar.DAY_OF_WEEK)-1] + ")");
					}
				};
				new DatePickerDialog(activity, callBack, calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)).show();
			}
		});
		
		button_add_time.setOnClickListener(new OnClickListener()
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
						button_add_time.setText(am_pm[ampm] + " " +
								((hourOfDay==0) ? 12 : hourOfDay%12) + ":" +
								((minute<10) ? "0" : "") + minute);
					}
				};
				new TimePickerDialog(activity, callBack, calendar.get(Calendar.HOUR_OF_DAY),
						calendar.get(Calendar.MINUTE), false).show();
			}
		});
		
		alertdialog = new AlertDialog.Builder(this)
			.setView(layout)
			.setTitle("�� �� �߰�")
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
						String todo_text = editText_add_todo.getText().toString();
						if(todo_text.equals(""))
						{
							new AlertDialog.Builder(activity)
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
									((calendar.get(Calendar.MINUTE)<10) ? "0" : "") +
									calendar.get(Calendar.MINUTE);
							ToDo todo = new ToDo();
							todo.setTodo(todo_text);
							todo.setCalendar(cal);
							todo.setCheck(check);
							todoAdapter.add(todo);
							alertdialog.dismiss();
						}
					}
				});
			}
		});
		alertdialog.show();
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		if(item.getItemId()==R.id.Menu_intro)
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("�Ұ�");
			
			alert.setMessage("�ܱ����б� 2015 â��������\n" +
				"â�����Ƹ� Wearoid\n" +
				"\n" +
				"���� ������ : ���絿 ������\n" +
				"å�� ������ : �̿��� ������\n" +
				"��ǥ�� : �ְ���\n" +
				"\n" +
				"������ : Wearoid SmartWatch2 ��\n" +
				"������ ������ �̼��� ������\n" +
				"\n" +
				"\nVersion 4.3.1");
			alert.show();
		}
		else if(item.getItemId()==R.id.Menu_help)			
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("����");
			
			alert.setMessage("���� ��ư�� ������\n" +
				"�� ��/�� �� ��ȯ�˴ϴ�.\n" +
				"\n" +
				"<�ȵ���̵�>\n" +
				"����Ʈ ������ ª�� ������\n" +
				"�� ������ Ȯ���� �� �ֽ��ϴ�.\n" +
				"����Ʈ ������ ��� ������\n" +
				"������ ������ �� �ֽ��ϴ�.\n" +
				"\n" +
				"<����Ʈ��ġ2>\n" +
				"�� �ϸ� ǥ�õ˴ϴ�.\n" +
				"����Ʈ ������ ������\n" +
				"�� ������ Ȯ���� �� �ֽ��ϴ�." +
				"\n");
			alert.show();
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
}