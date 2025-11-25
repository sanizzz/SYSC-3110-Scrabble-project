import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class BoardConfigLoader {
    private BoardConfigLoader() {}

    public static BoardLibrary loadLibrary(Path directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("directory cannot be null");
        }
        if (!Files.exists(directory)) {
            throw new IOException("Board directory not found: " + directory);
        }

        List<BoardLayout> layouts = new ArrayList<>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            try (Stream<Path> files = Files.list(directory)) {
                files.filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                        .sorted()
                        .forEach(path -> {
                            try {
                                layouts.add(parseLayout(builder, path));
                            } catch (Exception ex) {
                                throw new RuntimeException("Failed to parse board file " + path + ": " + ex.getMessage(), ex);
                            }
                        });
            }
        } catch (ParserConfigurationException e) {
            throw new IOException("Failed to initialize XML parser", e);
        } catch (RuntimeException e) {
            throw new IOException(e.getMessage(), e);
        }

        if (layouts.isEmpty()) {
            throw new IOException("No board layouts found in " + directory);
        }
        return new BoardLibrary(layouts);
    }

    private static BoardLayout parseLayout(DocumentBuilder builder, Path file) throws Exception {
        Document document = builder.parse(file.toFile());
        Element root = document.getDocumentElement();
        String name = root.getAttribute("name");
        if (name == null || name.isEmpty()) {
            name = file.getFileName().toString();
        }
        int size = 15;
        String sizeAttr = root.getAttribute("size");
        if (!sizeAttr.isEmpty()) {
            size = Integer.parseInt(sizeAttr);
        }

        PremiumSquare[][] grid = new PremiumSquare[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = PremiumSquare.NORMAL;
            }
        }

        NodeList premiums = root.getElementsByTagName("premium");
        for (int i = 0; i < premiums.getLength(); i++) {
            Node node = premiums.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element premiumElement = (Element) node;
            int row = Integer.parseInt(premiumElement.getAttribute("row"));
            int col = Integer.parseInt(premiumElement.getAttribute("col"));
            if (row < 0 || row >= size || col < 0 || col >= size) {
                throw new IllegalArgumentException("Premium square out of bounds in " + name);
            }
            PremiumSquare square = PremiumSquare.fromToken(premiumElement.getAttribute("type"));
            grid[row][col] = square;
        }
        return new BoardLayout(name, grid);
    }

    public static final class BoardLayout {
        private final String name;
        private final PremiumSquare[][] squares;

        public BoardLayout(String name, PremiumSquare[][] squares) {
            this.name = name;
            this.squares = new PremiumSquare[squares.length][squares[0].length];
            for (int r = 0; r < squares.length; r++) {
                System.arraycopy(squares[r], 0, this.squares[r], 0, squares[r].length);
            }
        }

        public String getName() {
            return name;
        }

        public int size() {
            return squares.length;
        }

        public PremiumSquare[][] copySquares() {
            PremiumSquare[][] copy = new PremiumSquare[squares.length][squares[0].length];
            for (int r = 0; r < squares.length; r++) {
                System.arraycopy(squares[r], 0, copy[r], 0, squares[r].length);
            }
            return copy;
        }
    }

    public static final class BoardLibrary {
        private final Map<String, BoardLayout> layouts = new LinkedHashMap<>();

        private BoardLibrary(List<BoardLayout> layouts) {
            for (BoardLayout layout : layouts) {
                this.layouts.put(layout.getName(), layout);
            }
        }

        public List<String> getBoardNames() {
            return Collections.unmodifiableList(new ArrayList<>(layouts.keySet()));
        }

        public BoardLayout require(String name) {
            BoardLayout layout = layouts.get(name);
            if (layout == null) {
                throw new IllegalArgumentException("Unknown board layout: " + name);
            }
            return layout;
        }
    }
}
