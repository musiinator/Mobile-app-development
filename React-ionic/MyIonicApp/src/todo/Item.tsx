import React, { memo } from 'react';
import { IonItem, IonLabel } from '@ionic/react';
import { getLogger } from '../core';
import { ItemProps } from './ItemProps';

const log = getLogger('Item');

interface ItemPropsExt extends ItemProps {
  onEdit: (id?: string) => void;
}

const Item: React.FC<ItemPropsExt> = ({ id, text, price, date, inStock, onEdit }) => {
  return (
    <IonItem onClick={() => onEdit(id)}>
      <IonLabel>Name: {text}</IonLabel>
      <IonLabel>Price: {price} RON</IonLabel>
      <IonLabel>Release date: {new Date(date).toLocaleString()}</IonLabel>
      <IonLabel>In stock: {inStock.toString()}</IonLabel>
    </IonItem>
  );
};

export default memo(Item);
