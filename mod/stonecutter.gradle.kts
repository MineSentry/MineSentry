plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "fabric-26.2"

stonecutter parameters {
    constants.match(
        current.project.substringAfterLast('-'),
        "fabric",
        "neoforge"
    )
}