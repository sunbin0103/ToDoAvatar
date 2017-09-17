/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2014, Sony Mobile Communications AB

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

package com.example.sonymobile.smartextension.controls;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.example.sonymobile.smartextension.todolist.R;
import com.example.sonymobile.smartextension.todolist.ToDoListExtensionService;
import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;

public class ToDoListControl extends ControlManagerBase
{
	private Stack<Intent> mControlStack;
	
	public ToDoListControl(final String hostAppPackageName, final Context context,
			Handler handler)
	{
		super(context, hostAppPackageName);
		if (handler == null)
			throw new IllegalArgumentException("handler == null");
		
		mControlStack = new Stack<Intent>();
		// Create an initial control extension
		Intent initialListControlIntent = new Intent(mContext, ListControlExtension.class);
		mCurrentControl = createControl(initialListControlIntent);
	}
	
	/**
	 * Get supported control width.
	 * 
	 * @param context The context.
	 * @return the width.
	 */
	public static int getSupportedControlWidth(Context context)
	{
		return context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_2_control_width);
	}
	
	/**
	 * Get supported control height.
	 * 
	 * @param context The context.
	 * @return the height.
	 */
	public static int getSupportedControlHeight(Context context)
	{
		return context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_2_control_height);
	}
	
	@Override
	public void onRequestListItem(int layoutReference, int listItemPosition)
	{
		Log.v(ToDoListExtensionService.LOG_TAG, "onRequestListItem");
		if (mCurrentControl != null)
			mCurrentControl.onRequestListItem(layoutReference, listItemPosition);
	}
	
	@Override
	public void onListItemClick(ControlListItem listItem, int clickType,
			int itemLayoutReference)
	{
		Log.v(ToDoListExtensionService.LOG_TAG, "onListItemClick");
		if(mCurrentControl != null)
			mCurrentControl.onListItemClick(listItem, clickType, itemLayoutReference);
	}
	
	@Override
	public void onListItemSelected(ControlListItem listItem)
	{
		Log.v(ToDoListExtensionService.LOG_TAG, "onListItemSelected");
		if(mCurrentControl != null)
			mCurrentControl.onListItemSelected(listItem);
	}
	
	@Override
	public void onListRefreshRequest(int layoutReference)
	{
		Log.v(ToDoListExtensionService.LOG_TAG, "onListRefreshRequest");
		if(mCurrentControl != null)
			mCurrentControl.onListRefreshRequest(layoutReference);
	}
	
	@Override
	public void onObjectClick(ControlObjectClickEvent event)
	{
		Log.v(ToDoListExtensionService.LOG_TAG, "onObjectClick");
		if(mCurrentControl != null)
			mCurrentControl.onObjectClick(event);
	}
	
	@Override
	public void onKey(int action, int keyCode, long timeStamp)
	{
		Log.v(ToDoListExtensionService.LOG_TAG, "onKey");
		
		if(action==Control.Intents.KEY_ACTION_RELEASE &&
				keyCode==Control.KeyCodes.KEYCODE_BACK)
		{
			Log.d(ToDoListExtensionService.LOG_TAG, "onKey() - back button intercepted.");
			onBack();
		}
		else if(action==Control.Intents.KEY_ACTION_RELEASE &&
				keyCode==Control.KeyCodes.KEYCODE_OPTIONS)
		{
			Intent intent = new Intent(
					"com.example.sonymobile.smartextension.todolist.ACTION_UPDATE_ACTIVITY");
			
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
			try {
				mContext.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(ToDoListExtensionService.LOG_TAG, e.toString());
			}
		}
		else if (mCurrentControl != null)
			super.onKey(action, keyCode, timeStamp);
	}
	
	@Override
	public void onMenuItemSelected(int menuItem)
	{
		Log.v(ToDoListExtensionService.LOG_TAG, "onMenuItemSelected");
		if (mCurrentControl != null)
			mCurrentControl.onMenuItemSelected(menuItem);
	}
	
	/**
	 * Closes the currently open control extension. If there is a control on the
	 * back stack it is opened, otherwise extension is closed.
	 */
	public void onBack()
	{
		Log.v(ToDoListExtensionService.LOG_TAG, "onBack");
		if(!mControlStack.isEmpty())
		{
			Intent backControl = mControlStack.pop();
			ControlExtension newControl = createControl(backControl);
			startControl(newControl);
		}
		else
			stopRequest();
	}
	
	/**
	 * Start a new control. Any currently running control will be stopped and
	 * put on the control extension stack.
	 * 
	 * @param intent the Intent used to create the ManagedControlExtension. The
	 * 		  intent must have the requested ManagedControlExtension as component,
	 * 		  e.g. Intent intent = new Intent(mContext, CallLogDetailsControl.class);
	 */
	public void startControl(Intent intent)
	{
		addCurrentToControlStack();
		ControlExtension newControl = createControl(intent);
		startControl(newControl);
	}
	
	public void addCurrentToControlStack()
	{
		if(mCurrentControl != null && mCurrentControl instanceof ManagedControlExtension)
		{
			Intent intent = ((ManagedControlExtension) mCurrentControl).getIntent();
			boolean isNoHistory = intent.getBooleanExtra(
					ManagedControlExtension.EXTENSION_NO_HISTORY, false);
			if (isNoHistory)
				// Not adding this control to history
				Log.d(ToDoListExtensionService.LOG_TAG, "Not adding control to history stack");
			else
			{
				Log.d(ToDoListExtensionService.LOG_TAG, "Adding control to history stack");
				mControlStack.add(intent);
			}
		}
		else
			Log.w(ToDoListExtensionService.LOG_TAG,
					"ControlManageronly supports ManagedControlExtensions");
	}
	
	private ControlExtension createControl(Intent intent)
	{
		ComponentName component = intent.getComponent();
		try
		{
			String className = component.getClassName();
			Log.d(ToDoListExtensionService.LOG_TAG, "Class name:" + className);
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor(Context.class, String.class,
					ToDoListControl.class, Intent.class);
			if(ctor == null)
                return null;
			Object object = ctor.newInstance(new Object[]
					{ mContext, mHostAppPackageName, this, intent });
			if(object instanceof ManagedControlExtension)
				return (ManagedControlExtension) object;
			else
				Log.w(ToDoListExtensionService.LOG_TAG,
						"Created object not a ManagedControlException");
		} catch (SecurityException e) {
			Log.w(ToDoListExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (NoSuchMethodException e) {
			Log.w(ToDoListExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (IllegalArgumentException e) {
			Log.w(ToDoListExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (InstantiationException e) {
			Log.w(ToDoListExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (IllegalAccessException e) {
			Log.w(ToDoListExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (InvocationTargetException e) {
			Log.w(ToDoListExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (ClassNotFoundException e) {
			Log.w(ToDoListExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		}
		return null;
	}
}