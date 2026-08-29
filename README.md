# Brahma Jarvis (Standalone Android Assistant)

Ye ek standalone Jarvis-type voice assistant hai jo poori tarah phone pe chalta hai —
kisi PC/desktop app ki zaroorat nahi. AI replies ke liye Google Gemini API (free tier
available) use hota hai.

## Features
- Chat UI (text + voice input/output)
- Voice input: Android SpeechRecognizer se bolke poocho
- Voice output: Text-to-Speech se assistant bol ke jawab deta hai
- Instant local commands (bina internet/AI ke): flashlight on/off, volume up/down/mute,
  battery %, "open <site/app name>"
- Baaki sab kuch (general questions, conversation) Gemini API se aata hai
- API key phone pe hi encrypted store hoti hai (EncryptedSharedPreferences)

## Build karne ke steps

1. **Android Studio install karo** (free): https://developer.android.com/studio
2. Is poore `brahma-jarvis` folder ko Android Studio mein **Open** karo
   (File → Open → is folder ko select karo)
3. Pehli baar Gradle sync hone dो (internet chahiye, dependencies download hongi)
4. Top pe device dropdown se ek **emulator** banao (ya apna phone USB debugging se connect karo)
5. **Run ▶** button dabao — app phone/emulator pe install ho jayega

### Signed release APK banane ke liye (taaki doosron ko bhej sako)
1. Android Studio mein: **Build → Generate Signed Bundle / APK**
2. **APK** select karo → **Next**
3. Naya keystore banao (ek baar ka kaam, password yaad rakhna)
4. **release** build variant select karo → **Finish**
5. APK yahan milega: `app/release/app-release.apk`

## Pehli baar app kholne ke baad
1. Settings (top-right gear icon) mein jaake apna **Gemini API key** daalo
   - Free key yahan se lo: https://aistudio.google.com/apikey
2. Mic permission aur camera permission (flashlight ke liye) allow karo
3. Ab type karke ya mic dabake bol ke assistant se baat karo

## Notes
- Ye poori tarah offline nahi chalega — AI replies ke liye internet + Gemini API key chahiye.
  Sirf flashlight/volume/battery jaise local commands bina internet ke chalte hain.
- Agar chaho to `GeminiClient.kt` mein model name (`gemini-2.0-flash`) badal ke koi
  aur Gemini model bhi use kar sakte ho.
- Agar future mein wake-word ("Hey Brahma") ya background service chahiye, wo agla
  step hoga — abhi mic button dabake bolna padega.
