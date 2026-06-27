const fs = require('fs');
let content = fs.readFileSync('app/src/main/java/com/example/MainActivity.kt', 'utf8');

// Replace containerColor = Color.White
content = content.replace(/containerColor = Color\.White/g, 'containerColor = com.example.ui.theme.HighDensityCardBg');
// Replace focusedContainerColor = Color.White
content = content.replace(/focusedContainerColor = Color\.White/g, 'focusedContainerColor = com.example.ui.theme.HighDensityCardBg');
// Replace unfocusedContainerColor = Color.White
content = content.replace(/unfocusedContainerColor = Color\.White/g, 'unfocusedContainerColor = com.example.ui.theme.HighDensityCardBg');
// Replace .background(... else Color.White)
content = content.replace(/else Color\.White/g, 'else com.example.ui.theme.HighDensityCardBg');

fs.writeFileSync('app/src/main/java/com/example/MainActivity.kt', content);
console.log("Done");
