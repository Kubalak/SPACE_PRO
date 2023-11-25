package pl.psk.termdemo.uimanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psk.termdemo.model.color.ANSIColors;
import pl.psk.termdemo.model.keys.KeyInfo;
import pl.psk.termdemo.model.keys.KeyLabel;

import java.util.List;
import java.util.regex.Pattern;

public class UITabela implements UIComponent {
        Logger logger = LoggerFactory.getLogger(pl.psk.termdemo.uimanager.UITabela.class);

        private StringBuilder textContent;
        private int x, y, width, height;
        private String bgColor = ANSIColors.BG_RED.getCode();
        private String textColor = ANSIColors.TEXT_WHITE.getCode();
        private boolean isNumeric, isPassword;
        private Pattern regexPattern = null;
        private int zIndex;
        private boolean isActive;
        private UIManager uiManager;
        private int maxCharacters = Integer.MAX_VALUE;

        // UIBorder = prostokacik w tabeli
        private List<UIBorder> borders;

        //UILabel teksty nad tabela
        private List<UILabel> labels;

        public UITabela(int x, int y, int width, int height, int zIndex, UIManager uiManager, List<UIBorder> borders, List<UILabel> labels) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.zIndex = zIndex;
            this.uiManager = uiManager;
            this.textContent = new StringBuilder();
            this.borders = borders;
            this.labels = labels;
        }

        public void drawAllElements(UITab uiTab) {

            this.labels.forEach(uiTab::addComponent);
            this.borders.forEach(uiTab::addComponent);
        }

        public void setMaxCharacters(int maxCharacters) {
            this.maxCharacters = maxCharacters;
        }

        public void setNumeric(boolean numeric) {
            isNumeric = numeric;
        }

        public void setPassword(boolean password) {
            isPassword = password;
        }

        public void setRegexPattern(String regex) {
            this.regexPattern = Pattern.compile(regex);
        }

        public String getText() {
            return textContent.toString();
        }

        @Override
        public void draw(UIManager uiManager) {

            String fullText = isPassword ? "*".repeat(textContent.length()) : textContent.toString();
            String displayText = fullText;

            if (fullText.length() > width) {
                displayText = fullText.substring(fullText.length() - width);
            }
            // Czyść cały obszar pola tekstowego
            for (int i = 0; i < width; i++) {
                ScreenCell emptyCell = new ScreenCell(' ', textColor, bgColor);  // Użyj pustego znaku do wyczyszczenia
                uiManager.getScreen().addPixelToLayer(x + i, y, zIndex, emptyCell);
            }

            // Następnie dodaj nowy tekst
            for (int i = 0; i < displayText.length(); i++) {
                ScreenCell cell = new ScreenCell(displayText.charAt(i), textColor, bgColor);
                uiManager.getScreen().addPixelToLayer(x + i, y, zIndex, cell);
            }
        }

        @Override
        public boolean isInside(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height);
        }

        public void appendText(KeyInfo keyInfo) {
            logger.debug("Appending text: {}", keyInfo.getValue());

            // Sprawdzenie kryteriów przed dodaniem znaku
            String newChar = keyInfo.getValue();

            // Jeżeli pole ma być numeryczne, ale znak nie jest cyfrą
            if(keyInfo.getLabel() != KeyLabel.DELETE) {
                // Jeśli znak nie jest cyfrą ani kropką
                if (isNumeric && !newChar.matches("\\d|\\.")) return;
                // Jeżeli znak nie pasuje do wzorca regex
                if (regexPattern != null && !newChar.matches(regexPattern.pattern())) return;
                // Jeśli kropka już istnieje
                if (isNumeric && textContent.indexOf(".") != -1 && keyInfo.getValue().equals(".")) return;
            }

            // Reszta logiki - kasowanie, dodawanie znaków itd.
            if (textContent.length() < maxCharacters || keyInfo.getLabel() == KeyLabel.DELETE) {
                if (keyInfo.getLabel() == KeyLabel.DELETE) {
                    if (!textContent.isEmpty()) {
                        textContent.deleteCharAt(textContent.length() - 1);

                        uiManager.render();
                        uiManager.refresh();
                    }
                    return;
                }

                if (keyInfo.getLabel() == KeyLabel.ENTER) {
                    performAction();
                    return;
                }

                // Dodanie nowego znaku
                textContent.append(keyInfo.getValue());

                // Ograniczenie długości tekstu do maxCharacters
                if (textContent.length() > maxCharacters)
                    textContent.deleteCharAt(0); // usuń pierwszy znak

                uiManager.render();
                uiManager.refresh();
            }
        }



        @Override
        public void performAction() {
            logger.debug("Performing action");
            logger.debug("Text content: {}", textContent.toString());
        }

        @Override
        public void show() {
            if (uiManager != null) {
                uiManager.registerUIComponent(this);
                uiManager.addComponentToScreen(this);
            }
        }

        @Override
        public void hide() {
            if (uiManager != null) {
                uiManager.unregisterUIComponent(this);
                uiManager.removeComponent(this);
            }
        }

        @Override
        public int getX() {return x;}

        @Override
        public int getY() {return y;}

        @Override
        public int getWidth() {return width;}

        @Override
        public int getHeight() {return height;}

        @Override
        public void setActive(boolean active) {this.isActive = active;}

        @Override
        public boolean isActive() {return this.isActive;}

        @Override
        public void highlight() {

            setBgColor(ANSIColors.BG_YELLOW.getCode());
            setTextColor(ANSIColors.TEXT_BLACK.getCode());

            uiManager.render();
            uiManager.refresh();
        }

        @Override
        public void resetHighlight() {
            setBgColor(ANSIColors.BG_RED.getCode());
            setTextColor(ANSIColors.TEXT_WHITE.getCode());

            uiManager.render();
            uiManager.refresh();
        }

        @Override
        public boolean isInteractable() {return true;}


        @Override
        public int getZIndex() {return zIndex;}

        public void setBgColor(String bgColor) {this.bgColor = bgColor;}

        public void setTextColor(String textColor) {this.textColor = textColor;}
    }