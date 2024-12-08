package io.github.etheradon.graalbridge.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// Uses Java beacuse the Kotlin code doesn't work with Forge
public abstract class Namespace {

    private final String name;
    private final List<String> otherNames;
    private final List<String> allNames;

    protected Namespace(String name, String... otherNames) {
        this.name = name;
        this.otherNames = Arrays.asList(otherNames);
        List<String> allNamesTemp = new ArrayList<>();
        allNamesTemp.add(name);
        allNamesTemp.addAll(this.otherNames);
        this.allNames = Collections.unmodifiableList(allNamesTemp);
    }

    public String getName() {
        return name;
    }

    public List<String> getOtherNames() {
        return otherNames;
    }

    public List<String> getAllNames() {
        return allNames;
    }

    public static final Namespace OFFICIAL = new Namespace("official", "obf", "target") {
    };
    public static final Namespace MOJMAP = new Namespace("mojmap", "source") {
    };
    public static final Namespace SRG = new Namespace("srg") {
    };
    public static final Namespace INTERMEDIARY = new Namespace("intermediary") {
    };
    public static final Namespace YARN = new Namespace("fabric", "named") {
    };
    public static final Namespace HASHED = new Namespace("hashed") {
    };
    public static final Namespace QUILT = new Namespace("quilt", "named") {
    };

    public static class Custom extends Namespace {

        public Custom(String name, String... otherNames) {
            super(name, otherNames);
        }

    }

    public static final List<Namespace> VALUES = Collections.unmodifiableList(Arrays.asList(
            OFFICIAL, MOJMAP, SRG, INTERMEDIARY, YARN, HASHED, QUILT
    ));

    public static Namespace fromName(String name) {
        String lowerCaseName = name.toLowerCase();
        for (Namespace ns : VALUES) {
            if (ns.getAllNames().contains(lowerCaseName)) {
                return ns;
            }
        }
        return new Custom(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Namespace)) {
            return false;
        }
        Namespace namespace = (Namespace) o;
        return name.equals(namespace.name) && otherNames.equals(namespace.otherNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, otherNames);
    }

    @Override
    public String toString() {
        return "MappingType(namespace='" + name + "', otherNames=" + otherNames + ")";
    }

}
