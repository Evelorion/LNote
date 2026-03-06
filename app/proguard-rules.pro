# This file contains the rules for ProGuard/R8

# Keep the main activity
-keep class com.example.noteapp.MainActivity { *; }
-keep class com.example.noteapp.EditNoteActivity { *; }

# Keep Room classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# Keep LiveData and ViewModel
-keep class androidx.lifecycle.** { *; }

# Keep Serializable classes
-keep class com.example.noteapp.db.NoteEntity { *; }

# Suppress warnings
-dontwarn android.os.**
-dontwarn androidx.**
