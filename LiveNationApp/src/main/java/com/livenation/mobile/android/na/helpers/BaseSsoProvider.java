package com.livenation.mobile.android.na.helpers;

import android.app.Activity;

abstract class BaseSsoProvider<SessionType> implements UiApiSsoProvider {
	protected final ActivityProvider activityProvider;

	public BaseSsoProvider(ActivityProvider activityProvider) {
		this.activityProvider = activityProvider;
	}

	public Activity getActivity() {
		return activityProvider.getActivity();
	}

	abstract void destroySession(SessionType session);

	protected abstract static class BaseSessionState<SessionType> {
		protected final SessionPayload<SessionType> sessionPayload;
		protected final boolean allowForeground;
		protected final Activity activity;

		public BaseSessionState(Activity activity, boolean allowForeground,
				SessionPayload<SessionType> sessionPayload) {
			this.allowForeground = allowForeground;
			this.activity = activity;
			this.sessionPayload = sessionPayload;
		}
		
		abstract public void open();

		protected static abstract class SessionPayload<SessionType> implements Runnable {
			private SessionType session;
			private final SessionPayloadListener<SessionType> listener;
			
			public SessionPayload(SessionPayloadListener<SessionType> listener) {
				this.listener = listener;
			}
			
			public SessionType getSession() {
				return session;
			}
			
			public void setSession(SessionType session) {
				this.session = session;
			}

			public SessionPayloadListener<SessionType> getListener() {
				return listener;
			}
			
			abstract void onSessionFailed();
			
			abstract void onNoNetwork();
			
		}
		
		protected static interface SessionPayloadListener<SessionType> {
			void onPayloadComplete(SessionPayload<SessionType> payload);
		}
	}

}
