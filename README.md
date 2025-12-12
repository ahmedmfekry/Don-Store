# تطبيق إدارة مخزون الدم - Regional Blood Transfusion Center, Ismailia

تطبيق Android لإدارة مخزون التبرع بالدم في المركز الإقليمي لنقل الدم بالإسماعيلية.

## المميزات

1. **إضافة الأصناف**: واجهة لإضافة أسماء الأصناف التي يمكن استخدامها في القوائم الأخرى
2. **إضافة المخزون**: إضافة طلبيات جديدة للمخزن مع جميع التفاصيل
3. **صرف المخزون**: صرف الأصناف للحملات الخارجية
4. **المرتجع**: إضافة الأصناف المرتجعة من الحملات الخارجية
5. **المصادقة**: تسجيل الدخول بحساب Google
6. **قاعدة البيانات**: استخدام Firebase Firestore لحفظ البيانات
7. **الإشعارات**: تنبيهات تلقائية عند اقتراب انتهاء صلاحية الأصناف (قبل شهر)

## المتطلبات

- Android Studio Hedgehog | 2023.1.1 أو أحدث
- JDK 8 أو أحدث
- Firebase project مع:
  - Authentication (Google Sign-In)
  - Firestore Database
  - Cloud Messaging (للإشعارات)

## الإعداد

1. قم بإنشاء مشروع Firebase جديد
2. أضف تطبيق Android إلى مشروع Firebase
3. قم بتنزيل ملف `google-services.json` وضعه في مجلد `app/`
4. في Firebase Console:
   - فعّل Authentication واختر Google Sign-In
   - أنشئ Firestore Database
5. احصل على Web Client ID من Firebase Console:
   - Authentication > Sign-in method > Google > Web client ID
6. ضع Web Client ID في `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>
   ```

## البنية

- `LoginActivity`: تسجيل الدخول بحساب Google
- `MainActivity`: الواجهة الرئيسية مع القوائم
- `AddItemActivity`: إضافة أصناف جديدة
- `AddStockActivity`: إضافة مخزون (طلبيات)
- `DispenseStockActivity`: صرف مخزون للحملات
- `ReturnStockActivity`: إضافة المرتجع
- `ExpirationNotificationService`: خدمة فحص انتهاء الصلاحية والإشعارات

## قاعدة البيانات

### Collection: `items`
- `name`: اسم الصنف
- `userId`: معرف المستخدم
- `createdAt`: وقت الإنشاء

### Collection: `stock`
- `date`: التاريخ
- `itemName`: اسم الصنف
- `quantity`: العدد
- `unit`: الوحدة
- `lotNumber`: رقم LOT
- `expireDate`: تاريخ الانتهاء
- `notes`: ملاحظات (اختياري)
- `userId`: معرف المستخدم
- `type`: نوع العملية (add, dispense, return)
- `createdAt`: وقت الإنشاء

## الإشعارات

يتم فحص الأصناف يومياً في الساعة 9 صباحاً. إذا كان هناك أصناف تنتهي صلاحيتها خلال 30 يوم، سيتم إرسال إشعار.

## الترخيص

هذا المشروع مخصص للاستخدام في المركز الإقليمي لنقل الدم بالإسماعيلية.

