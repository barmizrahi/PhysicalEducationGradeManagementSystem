# הערת אבטחה חשובה 🔒

## משתני סביבה רגישים

הפרויקט דורש משתני סביבה רגישים שלא נכללים ב-repository מסיבות אבטחה.

### להגדרה מקומית (Development)

1. העתק את `frontend/.env.example` ל-`frontend/.env`
2. מלא את הערכים האמיתיים:
   ```
   VITE_GOOGLE_CLIENT_ID=<Your Google Client ID>
   ```

3. הגדר משתני סביבה ב-`application.properties` או כמשתני סביבה של המערכת:
   ```
   GOOGLE_CLIENT_ID=<Your Google Client ID>
   GOOGLE_CLIENT_SECRET=<Your Google Client Secret>
   ```

### לפריסה (Production)

כל משתני הסביבה הרגישים מוגדרים ב-Render.com Dashboard:

**Backend:**
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`
- `JWT_SECRET`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

**Frontend:**
- `VITE_GOOGLE_CLIENT_ID`
- `VITE_API_URL`

## איך להשיג Google OAuth Credentials

1. עבור ל-[Google Cloud Console](https://console.cloud.google.com/)
2. צור פרויקט חדש או בחר פרויקט קיים
3. הפעל את Google+ API
4. צור OAuth 2.0 Client ID:
   - Application type: Web application
   - Authorized JavaScript origins: 
     - `http://localhost:3000` (development)
     - `https://your-frontend-url.onrender.com` (production)
   - Authorized redirect URIs:
     - `http://localhost:8080/api/auth/google/callback` (development)
     - `https://your-backend-url.onrender.com/api/auth/google/callback` (production)

5. שמור את Client ID ו-Client Secret

## ⚠️ אל תעלה credentials ל-Git!

קבצים שלא צריכים להיות ב-Git:
- `frontend/.env` (רק `.env.example` צריך להיות)
- כל קובץ שמכיל סודות או מפתחות API

---

**הערה**: אם בטעות העלית credentials ל-Git, **שנה אותם מיד** ב-Google Cloud Console!
