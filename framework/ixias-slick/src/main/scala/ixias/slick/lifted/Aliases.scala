/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick.lifted

/** Aliases for lifted embedding features. This trait can be mixed into aliasing objects which simplify the use of the
  * lifted embedding.
  */
trait Aliases {
  val Cursor = ixias.model.Cursor
  type Cursor = ixias.model.Cursor
}
