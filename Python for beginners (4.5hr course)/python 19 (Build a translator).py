# Build a Translator


# Giraffe Language

# vowel -> g
# -----------------
# dog -> dgg


def translate(phrase):
    translation = ""
    for letter in phrase:
        # we're going to check if the letter is a vowel
        if letter.lower in "AEIOUaeiou":
            # if letter.isupper():
            #     translation = translation + "G"
            # else:
            translation = translation + "g"
        else:
            translation = translation + letter
    return translation
# the for loop is basically going through every letter of the word that is passed through
# the if statement is basically allowing for every vowel instance to be replaced by the letter g otherwise (else) it leaves the letter there

print(translate(input("Enter a phrase: ")))
# the output for dog is dgg














