# מדריך מהיר לפריסה ב-Render.com

## התחלה מהירה (5 דקות)

### 1. הכן את הקוד
```bash
git add .
git commit -m "Ready for Render deployment"
git push origin main
```

### 2. צור חשבון ב-Render
- עבור ל-https://render.com
- הירשם עם GitHub

### 3. פרוס עם Blueprint (אוטומטי!)
1. לחץ "New" → "Blueprint"
2. בחר את ה-repository שלך
3. Render יזהה את `render.yaml` ויצור הכל אוטומטית!
4. המתן 5-10 דקות לבנייה

### 4. הגדר משתני סביבה (חובה!)

לאחר שהשירותים נוצרו, עדכן את משתני הסביבה:

#### Backend Service:
```
GOOGLE_CLIENT_ID=<Your Google Client ID>
GOOGLE_CLIENT_SECRET=<Your Google Client Secret>
GOOGLE_REDIRECT_URI=https://YOUR-BACKEND-URL.onrender.com/api/auth/google/callback
```

#### Frontend Service:
```
VITE_GOOGLE_CLIENT_ID=<Your Google Client ID>
```

### 5. עדכן Google OAuth
1. עבור ל-[Google Cloud Console](https://console.cloud.google.com)
2. APIs & Services → Credentials
3. ערוך את ה-OAuth Client
4. הוסף Authorized redirect URIs:
   ```
   https://YOUR-BACKEND-URL.onrender.com/api/auth/google/callback
   ```
5. הוסף Authorized JavaScript origins:
   ```
   https://YOUR-FRONTEND-URL.onrender.com
   ```

### 6. אתחל את מסד הנתונים
1. התחבר למסד הנתונים דרך Render Dashboard
2. הרץ:
   ```sql
   -- מתוך database/insert_teacher.sql
   INSERT INTO teachers (email, full_name, created_at)
   VALUES ('your-email@gmail.com', 'Your Name', CURRENT_TIMESTAMP);
   ```

### 7. בדוק שהכל עובד!
- פתח את ה-URL של ה-Frontend
- התחבר עם Google
- נסה את כל הפונקציות

## זהו! 🎉

האתר שלך עכשיו חי ב-internet!

## URLs שתקבל:
- Frontend: `https://pe-grades-frontend.onrender.com`
- Backend: `https://pe-grades-backend.onrender.com`
- Database: Internal connection string

## חשוב לדעת:
- ⏰ Free tier "נרדם" אחרי 15 דקות - הטעינה הראשונה תהיה איטית
- 💾 יש לך 1GB storage במסד נתונים
- 🔄 השירות יתעדכן אוטומטית כשתעשה push ל-GitHub

## בעיות?
בדוק את ה-Logs בכל שירות:
Render Dashboard → Service → Logs

---

**טיפ**: שמור את ה-URLs שקיבלת במקום בטוח!
