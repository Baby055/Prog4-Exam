package school.hei.demo.file.hash;

import school.hei.demo.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
