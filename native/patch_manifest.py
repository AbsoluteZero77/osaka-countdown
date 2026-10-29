p = "android/app/src/main/AndroidManifest.xml"
s = open(p, encoding="utf-8").read()
r = '''
        <receiver android:name=".CountdownWidget" android:exported="true" android:label="Osaka Countdown">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/countdown_widget_info" />
        </receiver>
'''
if "CountdownWidget" not in s:
    s = s.replace("</application>", r + "    </application>", 1)
open(p, "w", encoding="utf-8").write(s)
print("manifest patched")
