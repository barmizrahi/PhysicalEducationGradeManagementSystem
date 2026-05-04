# הוראות פריסה ל-Render.com

## דרישות מוקדמות

1. חשבון ב-Render.com (ניתן להירשם בחינם)
2. חשבון GitHub (הפרויקט צריך להיות ב-repository)
3. Google OAuth credentials (אם רוצים שההתחברות תעבוד)

## שלבי הפריסה

### שלב 1: הכנת הפרויקט

1. וודא שכל השינויים נשמרו ב-Git:
```bash
git add .
git commit -m "Prepare for Render deployment"
git push origin main
```

### שלב 2: יצירת שירותים ב-Render

#### אופציה א': פריסה אוטומטית עם Blueprint (מומלץ)

1. היכנס ל-Render.com
2. לחץ על "New" → "Blueprint"
3. חבר את ה-GitHub repository שלך
4. Render יזהה את קובץ `render.yaml` ויצור את כל השירותים אוטומטית:
   - PostgreSQL Database
   - Backend (Spring Boot)
   - Frontend (React)

#### אופציה ב': פריסה ידנית

##### 1. יצירת מסד נתונים PostgreSQL

1. לחץ על "New" → "PostgreSQL"
2. שם: `pe-grades-db`
3. Database Name: `pe_grades`
4. User: `postgrest`
5. Region: Frankfurt (או קרוב אליך)
6. Plan: Free
7. לחץ "Create Database"
8. שמור את ה-Connection String (תצטרך אותו בהמשך)

##### 2. פריסת Backend

1. לחץ על "New" → "Web Service"
2. חבר את ה-GitHub repository
3. הגדרות:
   - Name: `pe-grades-backend`
   - Region: Frankfurt
   - Branch: `main`
   - Root Directory: (השאר ריק)
   - Runtime: Java
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/grade-management-system-0.0.1-SNAPSHOT.jar`
   - Plan: Free

4. Environment Variables (לחץ "Add Environment Variable"):
   ```
   SPRING_DATASOURCE_URL=<Connection String from PostgreSQL>
   SPRING_DATASOURCE_USERNAME=postgrest
   SPRING_DATASOURCE_PASSWORD=<Password from PostgreSQL>
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   SPRING_JPA_SHOW_SQL=false
   JWT_SECRET=<Generate a random 256-bit string>
   JWT_EXPIRATION=86400000
   GOOGLE_CLIENT_ID=<Your Google Client ID>
   GOOGLE_CLIENT_SECRET=<Your Google Client Secret>
   FRONTEND_URL=<Will be set after frontend deployment>
   LOG_LEVEL=INFO
   ```

5. לחץ "Create Web Service"

##### 3. פריסת Frontend

1. לחץ על "New" → "Static Site"
2. חבר את ה-GitHub repository
3. הגדרות:
   - Name: `pe-grades-frontend`
   - Region: Frankfurt
   - Branch: `main`
   - Root Directory: `frontend`
   - Build Command: `npm install && npm run build`
   - Publish Directory: `dist`
   - Plan: Free

4. Environment Variables:
   ```
   VITE_API_URL=<Backend URL from step 2>
   VITE_GOOGLE_CLIENT_ID=<Your Google Client ID>
   VITE_ENV=production
   ```

5. לחץ "Create Static Site"

### שלב 3: עדכון הגדרות CORS

1. חזור לשירות ה-Backend
2. עדכן את משתנה הסביבה `FRONTEND_URL` ל-URL של ה-Frontend
3. שמור ופרוס מחדש

### שלב 4: עדכון Google OAuth (אופציונלי)

אם רוצים שההתחברות עם Google תעבוד בפרודקשן:

1. היכנס ל-Google Cloud Console
2. עבור ל-APIs & Services → Credentials
3. ערוך את ה-OAuth 2.0 Client ID
4. הוסף Authorized redirect URIs:
   ```
   https://<backend-url>.onrender.com/api/auth/google/callback
   ```
5. הוסף Authorized JavaScript origins:
   ```
   https://<frontend-url>.onrender.com
   ```

### שלב 5: אתחול מסד הנתונים

1. התחבר למסד הנתונים דרך Render Dashboard
2. הרץ את הסקריפטים מתיקיית `database/`:
   - `create_tables_and_user.sql` (אם צריך)
   - `insert_teacher.sql` (ליצירת משתמש ראשוני)
   - `fix_raw_result_nullable.sql` (אם צריך)

## בדיקת הפריסה

1. פתח את ה-URL של ה-Frontend
2. נסה להתחבר עם Google
3. בדוק שכל הפונקציות עובדות:
   - ייבוא תלמידים
   - יצירת מבחנים
   - הזנת ציונים
   - ייצוא לאקסל

## טיפים

### Free Tier Limitations

- השירותים החינמיים של Render "נרדמים" אחרי 15 דקות של חוסר פעילות
- הטעינה הראשונה אחרי "שינה" יכולה לקחת 30-60 שניות
- יש מגבלה של 750 שעות חינם לחודש (מספיק לשירות אחד 24/7)

### שיפור ביצועים

1. שמור את השירותים "ערים" עם Uptime Robot (חינם)
2. שקול שדרוג ל-Paid Plan אם צריך זמינות 24/7

### Troubleshooting

אם משהו לא עובד:

1. בדוק את ה-Logs בכל שירות (Render Dashboard → Service → Logs)
2. וודא שכל משתני הסביבה מוגדרים נכון
3. בדוק שה-CORS מוגדר נכון (FRONTEND_URL)
4. וודא שה-Database Connection String נכון

## עלויות

- **Free Tier**: 
  - PostgreSQL: 1GB storage, 97 connection hours/month
  - Web Services: 750 hours/month
  - Static Sites: 100GB bandwidth/month
  - **עלות: $0/חודש**

- **Paid Plans** (אם צריך יותר):
  - PostgreSQL: מ-$7/חודש
  - Web Services: מ-$7/חודש
  - Static Sites: מ-$1/חודש

## תמיכה

אם יש בעיות, בדוק:
- [Render Documentation](https://render.com/docs)
- [Render Community](https://community.render.com)
- Logs בכל שירות

---

**הערה**: הפרויקט מוכן לפריסה! כל הקבצים והגדרות מוגדרים נכון.
