package TrainningRoom.HTTP;

import java.util.Arrays;

public class User {
    private String id;
    private String name;
    private int age;
    private String email;

    User() {};

    User(String idValue, String nameValue, int ageValue, String emailValue) {
        id = idValue;
        name = nameValue;
        age = ageValue;
        email = emailValue;
    }

    @Override
    public String toString() {
        return "{\n\"id\": \"" + id + "\", \n\"name\": \"" + name + "\", \n\"age\": " + age + ",\n\"email\": \"" + email + "\" \n}";
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public static User parseUserJson(String id, String json) {
        json = json.trim()
                .replace("{", "")
                .replace("}", "")
                .replace("\"", "");

        String[] fields = json.split(",");

        String name = "";
        int age = -1;
        String email = "";

        for (String field : fields) {
            System.err.println("log: " + field);

            String[] keyValue = field.split(":");

            if (keyValue.length < 2) continue;

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            switch (key) {
                case "name":
                    name = value;
                    break;
                case "age":
                    age = Integer.parseInt(value);
                    break;
                case "email":
                    email = value;
                    break;
            }
        }

        return new User(id, name, age, email);
    }

    public void updateUser(User user) {
        if (!user.name.isEmpty()) {
            name = user.name;
        }

        if (!user.email.isEmpty()) {
            email = user.email;
        }

        if (user.age != -1) {
            age = user.age;
        }
    }
}
