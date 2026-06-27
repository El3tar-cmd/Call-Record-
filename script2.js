const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/MainActivity.kt', 'utf8');

// Replace Color.White
content = content.replace(/Color\.White/g, 'com.example.ui.theme.HighDensityOnPrimary');

fs.writeFileSync('app/src/main/java/com/example/MainActivity.kt', content);
console.log("Done");
