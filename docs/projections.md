# Projections

It's mathematically imposible to create a perfect flat projection of a globe... and Minecraft is flat. Therefore, some distortion will always show when projecting the Earth. The level of this distortion varies based on the properties used for the project.

Generally, this means a trade-off between _shape accuracy_ and _area accuracy_. Any projection can be described as two equations relating longitude and latitude to the 2D `x` and `y` coordinates.

    λ = longitude
    φ = latitude

## [Equirectangular](https://en.wikipedia.org/wiki/Equirectangular_projection)

-   **Speed:** Fastest
-   **Shape distortion:** Medium
-   **Area distortion:** Medium

    x = λ
    y = φ

![Source: Wikipedia](https://upload.wikimedia.org/wikipedia/commons/8/83/Equirectangular_projection_SW.jpg)

This is a very simple projection that performs no transformation on the latitude and longitude. It's the fastest and simplest of these. It has fairly decent shape and area preservation as long as you're not near the poles.

## [Sinusoidal](https://en.wikipedia.org/wiki/Sinusoidal_projection)

-   **Speed:** Fast
-   **Shape distortion:** High
-   **Area distortion:** None

    x = λcosφ
    y = φ

![Source: Wikipedia](https://upload.wikimedia.org/wikipedia/commons/b/b9/Sinusoidal_projection_SW.jpg)

This is an equal-area projection, so any region will have the exact same area in it as it does on Earth, which comes at the cost of a high shape distortion. This projection is fairly fast and will give the best performance of any equal area projection.

## [Mercator](https://en.wikipedia.org/wiki/Mercator_projection)

-   **Speed:** Fast
-   **Shape distortion:** None
-   **Area distortion:** High

    x = λ in radians
    y = log(tan(½φ + 45°)))

![Source: Wikipedia](https://upload.wikimedia.org/wikipedia/commons/7/73/Mercator_projection_Square.JPG)

This is the most common projection and is similar to the ones used by OpenStreetMap, Google Maps, and most modern maps. The shapes (or angles) are perfect, but the areas are very far from the pole. It is also fairly slower than simpler projections.

## [Equal Earth](https://en.wikipedia.org/wiki/Equal_Earth_projection)

-   **Speed:** Medium
-   **Shape distortion:** High
-   **Area distortion:** None

<pre><code>x = (2 √3 λcosθ) / (3(9 A<sub>4</sub> θ<sup>8</sup> + 7 A<sub>3</sub> θ<sup>6</sup> + 3 A<sub>2</sub> θ<sup>2</sup> + A<sub>1</sub>)
y = A<sub>4</sub> θ<sup>9</sup> + A<sub>3</sub> θ<sup>7</sup> + A<sub>2</sub> θ<sup>3</sup> + A<sub>1</sub> θ</code></pre>

where

<pre><code>sinθ = (√3) / 2 sin φ
A<sub>1</sub> = 1.340264
A<sub>2</sub> = -0.081106
A<sub>3</sub> = 0.000893
A<sub>4</sub> = 0.003796</pre></code>

![Source: Wikipedia](https://upload.wikimedia.org/wikipedia/commons/6/61/Equal_Earth_projection_SW.jpg)

This is a relatively new projection. It maintains area but also tries to have less shape distortion than Sinusoidal. It was invented by Bojan Šavrič, Bernhard Jenny, and Tom Patterson in 2018. The formula is by far the most complicated and slowest of these—reversing it requires solving a 9th degree polynomial.

## Airocean

-   **Speed:** Very slow
-   **Shape distortion:** Low
-   **Area distortion:** Low

![Source: Wikipedia](https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Dymaxion_projection.png/1920px-Dymaxion_projection.png)

This is a relatively complex projection based on the [Dymaxion map](https://en.wikipedia.org/wiki/Dymaxion_map). It projects the Earth onto an icosahedron (a polyhedron with 20 faces), and then "unfolds" that shape. The shape is split in such a way so that the map is discontinous only over the ocean.

This map shows a more complete image of the Earth's landmasses with places like Antarctica shown completely in one appropriately sized region on the map. It doesn't have a perfect shape or area distortion but it has a very low overall distortion. The downside is that it is very very slow to compute compared to the above projections, and north is not always to the same direction.

## BTE Airocean

-   **Speed:** Very slow
-   **Shape Distortion:** Very low
-   **Area Distortion:** Medium

This projection is based on [Airocean](#Airocean) and Daniel Strebe's [Dymaxion-like conformal projection](https://map-projections.net/single-view/dymaxion-like-conformal). With Dann's help and that of the Build the Earth Development team, we were able to [develop the projection in just over a week](https://www.youtube.com/watch?v=0eyyuNvKNzw). We used a dataset from Daniel's projection to nearly replicate its conformal properties, meaning there is almost no shape distortion.

The difference between the Airocean and the BTE projections is that the latter flips the eastern hemisphere to partially correct north. It is the official projection used for the Build the Earth project.

![BTE projection](images/projections/bte_airocean.jpg)
