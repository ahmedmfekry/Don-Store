# دليل الإعداد - تطبيق إدارة مخزون الدم

## خطوات الإعداد

### 1. إنشاء مشروع Firebase

1. اذهب إلى [Firebase Console](https://console.firebase.google.com/)
2. انقر على "Add project" أو "إضافة مشروع"
3. أدخل اسم المشروع: `Blood Inventory`
4. اتبع الخطوات لإكمال إنشاء المشروع

### 2. إضافة تطبيق Android

1. في Firebase Console، انقر على أيقونة Android
2. أدخل:
   - **Package name**: `com.bloodinventory.bloodinventory`
   - **App nickname**: `Blood Inventory App`
   - **Debug signing certificate SHA-1**: (اختياري - للحصول عليه، انظر أدناه)
3. انقر "Register app"
4. قم بتنزيل ملف `google-services.json`
5. ضع الملف في مجلد `app/` في المشروع

### 3. الحصول على SHA-1 (اختياري)

#### Windows:
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

#### Mac/Linux:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

انسخ SHA-1 وأضفه في Firebase Console > Project Settings > Your apps

### 4. إعداد Authentication

1. في Firebase Console، اذهب إلى **Authentication**
2. انقر على **Get started**
3. في تبويب **Sign-in method**، فعّل **Google**
4. أدخل **Support email**
5. انقر **Save**
6. انسخ **Web client ID** من نفس الصفحة

### 5. إعداد Firestore Database

1. في Firebase Console، اذهب إلى **Firestore Database**
2. انقر على **Create database**
3. اختر **Start in test mode** (للبدء)
4. اختر موقع قاعدة البيانات (يفضل `europe-west` أو `us-central`)
5. انقر **Enable**

### 6. إعداد Cloud Messaging (للإشعارات)

1. في Firebase Console، اذهب إلى **Cloud Messaging**
2. سيكون مفعّل تلقائياً

### 7. تحديث Web Client ID في التطبيق

1. افتح `app/src/main/res/values/strings.xml`
2. ابحث عن:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>
   ```
3. استبدل `YOUR_WEB_CLIENT_ID` بـ Web Client ID الذي نسخته من Firebase

### 8. بناء التطبيق

1. افتح المشروع في Android Studio
2. انتظر حتى يتم تحميل جميع التبعيات
3. اضغط **Build > Make Project**
4. إذا ظهرت أخطاء، تأكد من:
   - وجود ملف `google-services.json` في مجلد `app/`
   - تحديث Web Client ID في `strings.xml`
   - تحديث Gradle و Android SDK

### 9. تشغيل التطبيق

1. قم بتوصيل جهاز Android أو استخدم محاكي
2. اضغط **Run > Run 'app'**
3. عند فتح التطبيق، ستحتاج لتسجيل الدخول بحساب Google

## قواعد Firestore (Security Rules)

بعد الإعداد الأولي، يجب تحديث قواعد الأمان:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /items/{itemId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
    match /stock/{stockId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
  }
}
```

## ملاحظات مهمة

- تأكد من أن جميع الحسابات المستخدمة في التطبيق مسجلة في Firebase Authentication
- قواعد Firestore في وضع "test mode" تسمح بالقراءة والكتابة لجميع المستخدمين لمدة 30 يوم
- بعد 30 يوم، يجب تحديث القواعد أو سيتوقف التطبيق عن العمل

## الدعم

إذا واجهت أي مشاكل:
1. تأكد من أن جميع الخطوات تمت بشكل صحيح
2. راجع سجلات الأخطاء في Android Studio
3. راجع Firebase Console للأخطاء

