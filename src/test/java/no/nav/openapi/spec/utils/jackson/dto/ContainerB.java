package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class ContainerB {
    @JsonProperty(value = "enum1")
    private ValueAnnotatedEnum enum1;

    @JsonProperty(value = "txt1")
    private String txt1;

    @JsonProperty(value = "renamedByAnnotation")
    private ValueAnnotatedEnum newName;

    public ValueAnnotatedEnum getEnum1() {
        return enum1;
    }

    public ContainerB() {
    }

    public ContainerB(ValueAnnotatedEnum enum1, String txt1, ValueAnnotatedEnum newName) {
        this.enum1 = enum1;
        this.txt1 = txt1;
        this.newName = newName;
    }

    public void setEnum1(ValueAnnotatedEnum enum1) {
        this.enum1 = enum1;
    }

    public String getTxt1() {
        return txt1;
    }

    public void setTxt1(String txt1) {
        this.txt1 = txt1;
    }

    public ValueAnnotatedEnum getNewName() {
        return newName;
    }

    public void setNewName(ValueAnnotatedEnum newName) {
        this.newName = newName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContainerB that)) return false;
        return enum1 == that.enum1 && Objects.equals(txt1, that.txt1) && Objects.equals(newName, that.newName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enum1, txt1, newName);
    }

    @Override
    public String toString() {
        return "ContainerB{" +
                "enum1=" + enum1 +
                ", txt1='" + txt1 + '\'' +
                ", newName='" + newName + '\'' +
                '}';
    }
}
